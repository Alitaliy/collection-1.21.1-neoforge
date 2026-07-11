$ErrorActionPreference = "Stop"

$dataVersion = 3955
$resourceRoot = Join-Path $PSScriptRoot "..\src\main\resources"
$structureRoot = Join-Path $resourceRoot "data\collection\structure\village"
$poolRoot = Join-Path $resourceRoot "data\minecraft\worldgen\template_pool\village"
$minecraftJar = Join-Path $env:USERPROFILE ".gradle\caches\neoformruntime\artifacts\minecraft_1.21.1_client.jar"

function Resolve-BlockName {
    param([string]$Name)
    if ($Name.Contains(":")) {
        return $Name
    }
    return "minecraft:$Name"
}

function Get-TagType {
    param([object]$Value)

    if ($Value -is [hashtable] -or $Value -is [System.Collections.Specialized.OrderedDictionary]) {
        return 10
    }
    if ($Value -is [System.Array]) {
        return 9
    }
    if ($Value -is [string]) {
        return 8
    }
    if ($Value -is [int] -or $Value -is [long]) {
        return 3
    }
    throw "Unsupported tag value type: $($Value.GetType().FullName)"
}

function Write-UInt16BE {
    param([System.IO.BinaryWriter]$Writer, [int]$Value)
    $bytes = [BitConverter]::GetBytes([uint16]$Value)
    [Array]::Reverse($bytes)
    $Writer.Write($bytes)
}

function Write-Int32BE {
    param([System.IO.BinaryWriter]$Writer, [int]$Value)
    $bytes = [BitConverter]::GetBytes([int]$Value)
    [Array]::Reverse($bytes)
    $Writer.Write($bytes)
}

function Write-StringTag {
    param([System.IO.BinaryWriter]$Writer, [string]$Value)
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($Value)
    Write-UInt16BE $Writer $bytes.Length
    $Writer.Write($bytes)
}

function Write-TagPayload {
    param([System.IO.BinaryWriter]$Writer, [int]$TagType, [object]$Value)

    switch ($TagType) {
        3 { Write-Int32BE $Writer ([int]$Value) }
        8 { Write-StringTag $Writer ([string]$Value) }
        9 {
            if ($Value.Length -eq 0) {
                $Writer.Write([byte]0)
                Write-Int32BE $Writer 0
                return
            }
            $childType = Get-TagType $Value[0]
            $Writer.Write([byte]$childType)
            Write-Int32BE $Writer $Value.Length
            foreach ($entry in $Value) {
                Write-TagPayload $Writer $childType $entry
            }
        }
        10 {
            foreach ($entry in $Value.GetEnumerator()) {
                $childType = Get-TagType $entry.Value
                $Writer.Write([byte]$childType)
                Write-StringTag $Writer $entry.Key
                Write-TagPayload $Writer $childType $entry.Value
            }
            $Writer.Write([byte]0)
        }
        default { throw "Unsupported payload tag type id: $TagType" }
    }
}

function Write-RootCompound {
    param([string]$FilePath, [hashtable]$RootTag)

    $directory = Split-Path -Parent $FilePath
    if (-not (Test-Path $directory)) {
        New-Item -ItemType Directory -Path $directory -Force | Out-Null
    }

    $fileStream = [System.IO.File]::Create($FilePath)
    try {
        $gzipStream = New-Object System.IO.Compression.GZipStream($fileStream, [System.IO.Compression.CompressionMode]::Compress)
        try {
            $writer = New-Object System.IO.BinaryWriter($gzipStream)
            try {
                $writer.Write([byte]10)
                Write-StringTag $writer ""
                Write-TagPayload $writer 10 $RootTag
            } finally {
                $writer.Dispose()
            }
        } finally {
            $gzipStream.Dispose()
        }
    } finally {
        $fileStream.Dispose()
    }
}

function New-Block {
    param(
        [int]$X,
        [int]$Y,
        [int]$Z,
        [string]$State,
        [object]$Nbt = $null,
        [hashtable]$Properties = @{}
    )

    return [pscustomobject]@{
        x = $X
        y = $Y
        z = $Z
        state = $State
        nbt = $Nbt
        properties = $Properties
    }
}

function New-BlockStateTag {
    param([object]$Block)

    $tag = [ordered]@{
        Name = Resolve-BlockName $Block.state
    }
    if ($Block.properties.Count -gt 0) {
        $properties = [ordered]@{}
        foreach ($entry in $Block.properties.GetEnumerator() | Sort-Object Name) {
            $properties[$entry.Key] = [string]$entry.Value
        }
        $tag["Properties"] = $properties
    }
    return $tag
}

function Get-BlockStateKey {
    param([object]$Block)

    if ($Block.properties.Count -eq 0) {
        return $Block.state
    }
    $parts = $Block.properties.GetEnumerator() | Sort-Object Name | ForEach-Object { "$($_.Key)=$($_.Value)" }
    return "$($Block.state)|$($parts -join ',')"
}

function New-JigsawNbt {
    param(
        [string]$Name,
        [string]$Target,
        [string]$Pool,
        [string]$FinalState,
        [string]$Joint
    )

    return [ordered]@{
        id = "minecraft:jigsaw"
        name = $Name
        target = $Target
        pool = $Pool
        final_state = $FinalState
        joint = $Joint
    }
}

function New-StructureTag {
    param(
        [int[]]$Size,
        [object[]]$Blocks
    )

    $palette = New-Object System.Collections.ArrayList
    $paletteIndex = @{}
    $serializedBlocks = New-Object System.Collections.ArrayList

    foreach ($block in $Blocks) {
        $stateKey = Get-BlockStateKey $block
        if (-not $paletteIndex.ContainsKey($stateKey)) {
            $paletteIndex[$stateKey] = $palette.Count
            [void]$palette.Add((New-BlockStateTag $block))
        }

        $entry = [ordered]@{
            pos = [int[]]@($block.x, $block.y, $block.z)
            state = [int]$paletteIndex[$stateKey]
        }
        if ($null -ne $block.nbt) {
            $entry["nbt"] = $block.nbt
        }
        [void]$serializedBlocks.Add($entry)
    }

    return [ordered]@{
        DataVersion = $dataVersion
        size = [int[]]@($Size)
        palette = $palette.ToArray()
        blocks = $serializedBlocks.ToArray()
        entities = @()
    }
}

function Add-BlockIfEmpty {
    param(
        [hashtable]$BlocksByPos,
        [object]$Block
    )

    $key = "$($Block.x),$($Block.y),$($Block.z)"
    if (-not $BlocksByPos.ContainsKey($key)) {
        $BlocksByPos[$key] = $Block
    }
}

function Set-Block {
    param(
        [hashtable]$BlocksByPos,
        [object]$Block
    )

    $BlocksByPos["$($Block.x),$($Block.y),$($Block.z)"] = $Block
}

function New-CollectorHouseBlocks {
    param([string]$Village)

    $blocks = @{}

    for ($x = 0; $x -lt 7; $x++) {
        for ($z = 0; $z -lt 7; $z++) {
            Set-Block $blocks (New-Block $x 0 $z "oak_planks")
            for ($y = 1; $y -lt 5; $y++) {
                Set-Block $blocks (New-Block $x $y $z "air")
            }
        }
    }

    foreach ($x in 0, 6) {
        foreach ($z in 0, 6) {
            for ($y = 1; $y -le 3; $y++) {
                Set-Block $blocks (New-Block $x $y $z "stripped_oak_log")
            }
        }
    }

    for ($x = 1; $x -le 5; $x++) {
        foreach ($z in 0, 6) {
            for ($y = 1; $y -le 3; $y++) {
                Set-Block $blocks (New-Block $x $y $z "oak_planks")
            }
        }
    }
    for ($z = 1; $z -le 5; $z++) {
        foreach ($x in 0, 6) {
            for ($y = 1; $y -le 3; $y++) {
                Set-Block $blocks (New-Block $x $y $z "oak_planks")
            }
        }
    }

    Set-Block $blocks (New-Block 0 1 3 "oak_door" $null @{ facing = "west"; half = "lower"; hinge = "left"; open = "false"; powered = "false" })
    Set-Block $blocks (New-Block 0 2 3 "oak_door" $null @{ facing = "west"; half = "upper"; hinge = "left"; open = "false"; powered = "false" })
    foreach ($pos in @(@(3, 2, 0), @(3, 2, 6), @(6, 2, 3))) {
        Set-Block $blocks (New-Block $pos[0] $pos[1] $pos[2] "glass_pane")
    }

    for ($x = 0; $x -lt 7; $x++) {
        for ($z = 0; $z -lt 7; $z++) {
            $roof = if ($x -eq 0 -or $x -eq 6 -or $z -eq 0 -or $z -eq 6) { "spruce_planks" } else { "oak_planks" }
            Set-Block $blocks (New-Block $x 4 $z $roof)
        }
    }

    Set-Block $blocks (New-Block 2 1 2 "collection:collector_workstation")
    Set-Block $blocks (New-Block 2 1 4 "bookshelf")
    Set-Block $blocks (New-Block 3 1 4 "barrel")
    Set-Block $blocks (New-Block 4 1 2 "white_bed" $null @{ facing = "east"; occupied = "false"; part = "foot" })
    Set-Block $blocks (New-Block 5 1 2 "white_bed" $null @{ facing = "east"; occupied = "false"; part = "head" })
    Set-Block $blocks (New-Block 4 1 4 "chest" $null @{ facing = "north"; type = "single"; waterlogged = "false" })
    Set-Block $blocks (New-Block 1 1 4 "flower_pot")

    Set-Block $blocks (New-Block 0 0 3 "jigsaw" (New-JigsawNbt "minecraft:building_entrance" "minecraft:building_entrance" "minecraft:village/$Village/streets" "minecraft:oak_planks" "aligned") @{ orientation = "west_up" })
    Set-Block $blocks (New-Block 3 0 3 "jigsaw" (New-JigsawNbt "minecraft:bottom" "minecraft:bottom" "minecraft:village/$Village/villagers" "minecraft:oak_planks" "rollable") @{ orientation = "up_north" })

    return @($blocks.Values)
}

function Write-CollectorHouse {
    param([string]$Village)

    $path = Join-Path $structureRoot "collector_house_$Village.nbt"
    Write-RootCompound $path (New-StructureTag -Size @(7, 5, 7) -Blocks (New-CollectorHouseBlocks $Village))
    Write-Host "generated $path"
}

function Add-CollectorHouseToPool {
    param([string]$Village)

    if (-not (Test-Path $minecraftJar)) {
        throw "Minecraft client jar not found: $minecraftJar"
    }

    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip = [System.IO.Compression.ZipFile]::OpenRead($minecraftJar)
    try {
        $entry = $zip.GetEntry("data/minecraft/worldgen/template_pool/village/$Village/houses.json")
        if ($null -eq $entry) {
            throw "Could not find vanilla village pool for $Village"
        }
        $reader = New-Object System.IO.StreamReader($entry.Open())
        try {
            $pool = $reader.ReadToEnd() | ConvertFrom-Json
        } finally {
            $reader.Dispose()
        }
    } finally {
        $zip.Dispose()
    }

    $collectorEntry = [pscustomobject]@{
        element = [pscustomobject]@{
            element_type = "minecraft:legacy_single_pool_element"
            location = "collection:village/collector_house_$Village"
            processors = "minecraft:empty"
            projection = "rigid"
        }
        weight = 2
    }

    $pool.elements = @($pool.elements) + $collectorEntry

    $outDir = Join-Path $poolRoot "$Village"
    if (-not (Test-Path $outDir)) {
        New-Item -ItemType Directory -Path $outDir -Force | Out-Null
    }
    $outPath = Join-Path $outDir "houses.json"
    $pool | ConvertTo-Json -Depth 64 | Set-Content -Path $outPath -Encoding UTF8
    Write-Host "generated $outPath"
}

foreach ($village in @("plains", "desert", "savanna", "snowy", "taiga")) {
    Write-CollectorHouse $village
    Add-CollectorHouseToPool $village
}
