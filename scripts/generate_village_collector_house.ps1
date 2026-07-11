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

function New-StairFinalState {
    param([string]$Name)

    return "$(Resolve-BlockName $Name)[facing=east,half=bottom,shape=straight,waterlogged=false]"
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

function New-LootChestNbt {
    return [ordered]@{
        id = "minecraft:chest"
        LootTable = "collection:chests/collector_house"
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

function Set-Block {
    param(
        [hashtable]$BlocksByPos,
        [object]$Block
    )

    $BlocksByPos["$($Block.x),$($Block.y),$($Block.z)"] = $Block
}

function Add-Post {
    param(
        [hashtable]$Blocks,
        [int]$X,
        [int]$Z,
        [string]$Block,
        [int]$Top = 4
    )

    for ($y = 1; $y -le $Top; $y++) {
        Set-Block $Blocks (New-Block $X $y $Z $Block)
    }
}

function Add-Carpet {
    param(
        [hashtable]$Blocks,
        [string]$Block
    )

    foreach ($pos in @(@(3, 1, 3), @(4, 1, 3), @(5, 1, 3), @(4, 1, 4))) {
        Set-Block $Blocks (New-Block $pos[0] $pos[1] $pos[2] $Block)
    }
}

function Add-CommonInterior {
    param(
        [hashtable]$Blocks,
        [hashtable]$Style
    )

    Set-Block $Blocks (New-Block 2 1 2 "collection:collector_workstation")
    Set-Block $Blocks (New-Block 6 1 2 "chest" (New-LootChestNbt) @{ facing = "west"; type = "single"; waterlogged = "false" })
    Set-Block $Blocks (New-Block 2 1 6 "bookshelf")
    Set-Block $Blocks (New-Block 3 1 6 "bookshelf")
    Set-Block $Blocks (New-Block 4 1 6 $Style.storage)
    Set-Block $Blocks (New-Block 5 1 5 $Style.bed $null @{ facing = "east"; occupied = "false"; part = "foot" })
    Set-Block $Blocks (New-Block 6 1 5 $Style.bed $null @{ facing = "east"; occupied = "false"; part = "head" })
    Set-Block $Blocks (New-Block 3 1 2 "chiseled_bookshelf" $null @{ facing = "south"; slot_0_occupied = "true"; slot_1_occupied = "false"; slot_2_occupied = "true"; slot_3_occupied = "false"; slot_4_occupied = "false"; slot_5_occupied = "true" })
    Set-Block $Blocks (New-Block 5 1 2 $Style.potted)
    Set-Block $Blocks (New-Block 3 2 2 "wall_torch" $null @{ facing = "south" })
    Set-Block $Blocks (New-Block 6 2 6 "wall_torch" $null @{ facing = "north" })
    Add-Carpet $Blocks $Style.carpet
}

function Add-SignatureDetails {
    param(
        [hashtable]$Blocks,
        [string]$Village,
        [hashtable]$Style
    )

    switch ($Village) {
        "plains" {
            Set-Block $Blocks (New-Block 1 1 1 "hay_block" $null @{ axis = "y" })
            Set-Block $Blocks (New-Block 1 1 6 "oak_fence")
        }
        "desert" {
            Set-Block $Blocks (New-Block 1 1 1 "chiseled_sandstone")
            Set-Block $Blocks (New-Block 1 1 6 "smooth_sandstone")
        }
        "savanna" {
            Set-Block $Blocks (New-Block 1 1 1 "orange_terracotta")
            Set-Block $Blocks (New-Block 1 1 6 "acacia_fence")
        }
        "snowy" {
            Set-Block $Blocks (New-Block 1 1 1 "packed_ice")
            Set-Block $Blocks (New-Block 1 1 6 "spruce_fence")
        }
        "taiga" {
            Set-Block $Blocks (New-Block 1 1 1 "mossy_cobblestone")
            Set-Block $Blocks (New-Block 1 1 6 "spruce_fence")
        }
    }
}

function New-CollectorHouseBlocks {
    param([string]$Village)

    $styles = @{
        plains = @{
            foundation = "cobblestone"; floor = "oak_planks"; wall = "white_terracotta"; accent = "oak_planks"; trim = "stripped_oak_log"; roof = "oak_planks"; roofAccent = "spruce_planks"; door = "oak_door"; window = "glass"; bed = "white_bed"; carpet = "green_carpet"; potted = "potted_dandelion"; storage = "oak_planks"; entryStair = "oak_stairs"
        }
        desert = @{
            foundation = "smooth_sandstone"; floor = "cut_sandstone"; wall = "sandstone"; accent = "smooth_sandstone"; trim = "cut_sandstone"; roof = "smooth_sandstone"; roofAccent = "terracotta"; door = "jungle_door"; window = "glass"; bed = "yellow_bed"; carpet = "orange_carpet"; potted = "potted_cactus"; storage = "chiseled_sandstone"; entryStair = "smooth_sandstone_stairs"
        }
        savanna = @{
            foundation = "cobblestone"; floor = "acacia_planks"; wall = "orange_terracotta"; accent = "acacia_planks"; trim = "stripped_acacia_log"; roof = "acacia_planks"; roofAccent = "dark_oak_planks"; door = "acacia_door"; window = "orange_stained_glass"; bed = "orange_bed"; carpet = "yellow_carpet"; potted = "potted_acacia_sapling"; storage = "acacia_planks"; entryStair = "acacia_stairs"
        }
        snowy = @{
            foundation = "cobblestone"; floor = "spruce_planks"; wall = "snow_block"; accent = "spruce_planks"; trim = "stripped_spruce_log"; roof = "spruce_planks"; roofAccent = "snow_block"; door = "spruce_door"; window = "glass"; bed = "light_blue_bed"; carpet = "light_blue_carpet"; potted = "potted_blue_orchid"; storage = "packed_ice"; entryStair = "spruce_stairs"
        }
        taiga = @{
            foundation = "mossy_cobblestone"; floor = "spruce_planks"; wall = "spruce_planks"; accent = "cobblestone"; trim = "stripped_spruce_log"; roof = "dark_oak_planks"; roofAccent = "spruce_planks"; door = "spruce_door"; window = "glass"; bed = "brown_bed"; carpet = "brown_carpet"; potted = "potted_fern"; storage = "spruce_planks"; entryStair = "spruce_stairs"
        }
    }

    $style = $styles[$Village]
    $blocks = @{}

    for ($x = 0; $x -lt 9; $x++) {
        for ($z = 0; $z -lt 9; $z++) {
            Set-Block $blocks (New-Block $x 0 $z $style.foundation)
            for ($y = 1; $y -lt 7; $y++) {
                Set-Block $blocks (New-Block $x $y $z "air")
            }
        }
    }

    for ($x = 1; $x -le 7; $x++) {
        for ($z = 1; $z -le 7; $z++) {
            Set-Block $blocks (New-Block $x 0 $z $style.floor)
        }
    }
    foreach ($z in 3, 4, 5) {
        Set-Block $blocks (New-Block 0 0 $z $style.floor)
    }

    foreach ($corner in @(@(1, 1), @(1, 7), @(7, 1), @(7, 7))) {
        Add-Post $blocks $corner[0] $corner[1] $style.trim 4
    }

    for ($x = 2; $x -le 6; $x++) {
        foreach ($z in 1, 7) {
            for ($y = 1; $y -le 3; $y++) {
                Set-Block $blocks (New-Block $x $y $z $style.wall)
            }
        }
    }
    for ($z = 2; $z -le 6; $z++) {
        foreach ($x in 1, 7) {
            for ($y = 1; $y -le 3; $y++) {
                Set-Block $blocks (New-Block $x $y $z $style.wall)
            }
        }
    }

    Set-Block $blocks (New-Block 1 1 4 $style.door $null @{ facing = "west"; half = "lower"; hinge = "left"; open = "false"; powered = "false" })
    Set-Block $blocks (New-Block 1 2 4 $style.door $null @{ facing = "west"; half = "upper"; hinge = "left"; open = "false"; powered = "false" })
    Set-Block $blocks (New-Block 1 3 4 $style.trim)

    foreach ($pos in @(@(4, 2, 1), @(4, 2, 7), @(7, 2, 4), @(1, 2, 2), @(1, 2, 6))) {
        Set-Block $blocks (New-Block $pos[0] $pos[1] $pos[2] $style.window)
    }

    for ($x = 0; $x -le 8; $x++) {
        for ($z = 0; $z -le 8; $z++) {
            if ($x -eq 0 -or $x -eq 8 -or $z -eq 0 -or $z -eq 8) {
                Set-Block $blocks (New-Block $x 4 $z $style.roofAccent)
            } else {
                Set-Block $blocks (New-Block $x 4 $z $style.roof)
            }
        }
    }
    for ($x = 2; $x -le 6; $x++) {
        for ($z = 2; $z -le 6; $z++) {
            Set-Block $blocks (New-Block $x 5 $z $style.roof)
        }
    }
    for ($x = 3; $x -le 5; $x++) {
        for ($z = 3; $z -le 5; $z++) {
            Set-Block $blocks (New-Block $x 6 $z $style.roofAccent)
        }
    }

    foreach ($pos in @(@(0, 1, 3), @(0, 1, 5), @(1, 1, 3), @(1, 1, 5))) {
        Set-Block $blocks (New-Block $pos[0] $pos[1] $pos[2] $style.accent)
    }
    Set-Block $blocks (New-Block 0 2 3 "lantern" $null @{ hanging = "false"; waterlogged = "false" })
    Set-Block $blocks (New-Block 0 2 5 "lantern" $null @{ hanging = "false"; waterlogged = "false" })

    Add-CommonInterior $blocks $style
    Add-SignatureDetails $blocks $Village $style

    Set-Block $blocks (New-Block 0 0 4 "jigsaw" (New-JigsawNbt "minecraft:building_entrance" "minecraft:building_entrance" "minecraft:village/$Village/streets" (New-StairFinalState $style.entryStair) "aligned") @{ orientation = "west_up" })
    Set-Block $blocks (New-Block 4 0 4 "jigsaw" (New-JigsawNbt "minecraft:bottom" "minecraft:bottom" "minecraft:village/$Village/villagers" (Resolve-BlockName $style.floor) "rollable") @{ orientation = "up_north" })

    return @($blocks.Values)
}

function Write-CollectorHouse {
    param([string]$Village)

    $path = Join-Path $structureRoot "collector_house_$Village.nbt"
    Write-RootCompound $path (New-StructureTag -Size @(9, 7, 9) -Blocks (New-CollectorHouseBlocks $Village))
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
        weight = 1
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
