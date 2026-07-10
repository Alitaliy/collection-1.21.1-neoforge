$ErrorActionPreference = "Stop"

$root = Join-Path $PSScriptRoot "..\src\main\resources\data\collection\structure"
$dataVersion = 3955

function New-IntArrayTag {
    param([int[]]$Values)

    return [ordered]@{
        __type = "int_array"
        values = $Values
    }
}

function Get-TagType {
    param([object]$Value)

    if ($Value -is [hashtable] -or $Value -is [System.Collections.Specialized.OrderedDictionary]) {
        if ($Value.Contains("__type") -and $Value["__type"] -eq "int_array") {
            return 11
        }
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
        3 {
            Write-Int32BE $Writer ([int]$Value)
        }
        8 {
            Write-StringTag $Writer ([string]$Value)
        }
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
        11 {
            $values = $Value["values"]
            Write-Int32BE $Writer $values.Length
            foreach ($number in $values) {
                Write-Int32BE $Writer ([int]$number)
            }
        }
        default {
            throw "Unsupported payload tag type id: $TagType"
        }
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

function New-BlockState {
    param([string]$Name)

    return [ordered]@{
        Name = "minecraft:$Name"
    }
}

function New-BrushableNbt {
    param([string]$LootTablePath)

    return [ordered]@{
        id = "minecraft:brushable_block"
        LootTable = "collection:archaeology/$LootTablePath"
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
        $stateName = $block.state
        if (-not $paletteIndex.ContainsKey($stateName)) {
            $paletteIndex[$stateName] = $palette.Count
            [void]$palette.Add((New-BlockState $stateName))
        }

        $entry = [ordered]@{
            pos = New-IntArrayTag @($block.x, $block.y, $block.z)
            state = [int]$paletteIndex[$stateName]
        }
        if ($null -ne $block.nbt) {
            $entry["nbt"] = $block.nbt
        }
        [void]$serializedBlocks.Add($entry)
    }

    return [ordered]@{
        DataVersion = $dataVersion
        size = New-IntArrayTag $Size
        palette = $palette.ToArray()
        blocks = $serializedBlocks.ToArray()
        entities = @()
    }
}

function New-Block {
    param(
        [int]$X,
        [int]$Y,
        [int]$Z,
        [string]$State,
        [object]$Nbt = $null
    )

    return [pscustomobject]@{
        x = $X
        y = $Y
        z = $Z
        state = $State
        nbt = $Nbt
    }
}

function Write-Site {
    param(
        [string]$RelativePath,
        [object[]]$Blocks
    )

    $fullPath = Join-Path $root "$RelativePath.nbt"
    Write-RootCompound $fullPath (New-StructureTag -Size @(5, 2, 5) -Blocks $Blocks)
    Write-Host "generated $fullPath"
}

$desertCommon = New-BrushableNbt "buried_coin_ruin_common"
$desertRare = New-BrushableNbt "buried_coin_ruin_rare"
$badlandsCommon = New-BrushableNbt "badlands_arrowhead_site_common"
$badlandsRare = New-BrushableNbt "badlands_arrowhead_site_rare"
$shorelineCommon = New-BrushableNbt "shoreline_relic_cache_common"
$shorelineRare = New-BrushableNbt "shoreline_relic_cache_rare"

$buriedCoinSites = @(
    @(
        (New-Block 0 0 0 "cut_sandstone"), (New-Block 0 0 1 "sandstone"), (New-Block 0 0 2 "chiseled_sandstone"), (New-Block 0 0 3 "sandstone"), (New-Block 0 0 4 "cut_sandstone"),
        (New-Block 1 0 0 "sandstone"), (New-Block 1 0 2 "suspicious_sand" $desertCommon), (New-Block 1 0 4 "sandstone"),
        (New-Block 2 0 0 "chiseled_sandstone"), (New-Block 2 0 1 "sand"), (New-Block 2 0 2 "suspicious_gravel" $desertRare), (New-Block 2 0 3 "sand"), (New-Block 2 0 4 "chiseled_sandstone"),
        (New-Block 3 0 0 "sandstone"), (New-Block 3 0 2 "suspicious_sand" $desertCommon), (New-Block 3 0 4 "sandstone"),
        (New-Block 4 0 0 "cut_sandstone"), (New-Block 4 0 1 "sandstone"), (New-Block 4 0 2 "chiseled_sandstone"), (New-Block 4 0 3 "sandstone"), (New-Block 4 0 4 "cut_sandstone"),
        (New-Block 1 1 1 "sandstone"), (New-Block 3 1 3 "sandstone")
    ),
    @(
        (New-Block 0 0 1 "cut_sandstone"), (New-Block 0 0 2 "sandstone"), (New-Block 0 0 3 "cut_sandstone"),
        (New-Block 1 0 0 "sandstone"), (New-Block 1 0 2 "suspicious_sand" $desertCommon), (New-Block 1 0 4 "sandstone"),
        (New-Block 2 0 0 "cut_sandstone"), (New-Block 2 0 1 "sand"), (New-Block 2 0 2 "chiseled_sandstone"), (New-Block 2 0 3 "suspicious_gravel" $desertRare), (New-Block 2 0 4 "cut_sandstone"),
        (New-Block 3 0 0 "sandstone"), (New-Block 3 0 2 "suspicious_sand" $desertCommon), (New-Block 3 0 4 "sandstone"),
        (New-Block 4 0 1 "cut_sandstone"), (New-Block 4 0 2 "sandstone"), (New-Block 4 0 3 "cut_sandstone"),
        (New-Block 1 1 3 "sandstone"), (New-Block 3 1 1 "sandstone")
    ),
    @(
        (New-Block 0 0 2 "chiseled_sandstone"),
        (New-Block 1 0 1 "sandstone"), (New-Block 1 0 2 "suspicious_sand" $desertCommon), (New-Block 1 0 3 "sandstone"),
        (New-Block 2 0 0 "chiseled_sandstone"), (New-Block 2 0 1 "cut_sandstone"), (New-Block 2 0 2 "sand"), (New-Block 2 0 3 "cut_sandstone"), (New-Block 2 0 4 "chiseled_sandstone"),
        (New-Block 3 0 1 "sandstone"), (New-Block 3 0 2 "suspicious_sand" $desertCommon), (New-Block 3 0 3 "sandstone"),
        (New-Block 4 0 2 "chiseled_sandstone"),
        (New-Block 2 1 1 "suspicious_gravel" $desertRare), (New-Block 2 1 3 "sandstone")
    )
)

$badlandsSites = @(
    @(
        (New-Block 0 0 0 "red_sandstone"), (New-Block 0 0 1 "terracotta"), (New-Block 0 0 2 "cut_red_sandstone"), (New-Block 0 0 3 "terracotta"), (New-Block 0 0 4 "red_sandstone"),
        (New-Block 1 0 0 "terracotta"), (New-Block 1 0 2 "suspicious_gravel" $badlandsCommon), (New-Block 1 0 4 "terracotta"),
        (New-Block 2 0 0 "cut_red_sandstone"), (New-Block 2 0 1 "red_sand"), (New-Block 2 0 2 "gravel"), (New-Block 2 0 3 "suspicious_sand" $badlandsRare), (New-Block 2 0 4 "cut_red_sandstone"),
        (New-Block 3 0 0 "terracotta"), (New-Block 3 0 2 "suspicious_gravel" $badlandsCommon), (New-Block 3 0 4 "terracotta"),
        (New-Block 4 0 0 "red_sandstone"), (New-Block 4 0 1 "terracotta"), (New-Block 4 0 2 "cut_red_sandstone"), (New-Block 4 0 3 "terracotta"), (New-Block 4 0 4 "red_sandstone")
    ),
    @(
        (New-Block 0 0 2 "red_sandstone"),
        (New-Block 1 0 1 "orange_terracotta"), (New-Block 1 0 2 "suspicious_gravel" $badlandsCommon), (New-Block 1 0 3 "orange_terracotta"),
        (New-Block 2 0 0 "red_sandstone"), (New-Block 2 0 1 "suspicious_gravel" $badlandsCommon), (New-Block 2 0 2 "gravel"), (New-Block 2 0 3 "red_sand"), (New-Block 2 0 4 "red_sandstone"),
        (New-Block 3 0 1 "brown_terracotta"), (New-Block 3 0 2 "suspicious_sand" $badlandsRare), (New-Block 3 0 3 "brown_terracotta"),
        (New-Block 4 0 2 "red_sandstone"),
        (New-Block 2 1 0 "cut_red_sandstone"), (New-Block 2 1 4 "cut_red_sandstone")
    ),
    @(
        (New-Block 0 0 1 "red_sandstone"), (New-Block 0 0 3 "red_sandstone"),
        (New-Block 1 0 0 "orange_terracotta"), (New-Block 1 0 1 "red_sand"), (New-Block 1 0 2 "suspicious_gravel" $badlandsCommon), (New-Block 1 0 3 "red_sand"), (New-Block 1 0 4 "brown_terracotta"),
        (New-Block 2 0 1 "red_sand"), (New-Block 2 0 2 "suspicious_sand" $badlandsRare), (New-Block 2 0 3 "red_sand"),
        (New-Block 3 0 0 "brown_terracotta"), (New-Block 3 0 1 "red_sand"), (New-Block 3 0 2 "suspicious_gravel" $badlandsCommon), (New-Block 3 0 3 "red_sand"), (New-Block 3 0 4 "orange_terracotta"),
        (New-Block 4 0 1 "red_sandstone"), (New-Block 4 0 3 "red_sandstone")
    )
)

$shorelineSites = @(
    @(
        (New-Block 0 0 0 "cobblestone"), (New-Block 0 0 4 "cobblestone"),
        (New-Block 1 0 1 "sandstone"), (New-Block 1 0 2 "suspicious_sand" $shorelineCommon), (New-Block 1 0 3 "sandstone"),
        (New-Block 2 0 1 "gravel"), (New-Block 2 0 2 "sand"), (New-Block 2 0 3 "suspicious_sand" $shorelineRare),
        (New-Block 3 0 1 "sandstone"), (New-Block 3 0 2 "suspicious_gravel" $shorelineCommon), (New-Block 3 0 3 "sandstone"),
        (New-Block 4 0 0 "cobblestone"), (New-Block 4 0 4 "cobblestone"),
        (New-Block 2 1 0 "mossy_cobblestone")
    ),
    @(
        (New-Block 0 0 2 "cobblestone"),
        (New-Block 1 0 1 "sandstone"), (New-Block 1 0 3 "sandstone"),
        (New-Block 2 0 0 "mossy_cobblestone"), (New-Block 2 0 1 "suspicious_sand" $shorelineCommon), (New-Block 2 0 2 "sand"), (New-Block 2 0 3 "suspicious_gravel" $shorelineCommon), (New-Block 2 0 4 "mossy_cobblestone"),
        (New-Block 3 0 1 "sandstone"), (New-Block 3 0 2 "suspicious_sand" $shorelineRare), (New-Block 3 0 3 "sandstone"),
        (New-Block 4 0 2 "cobblestone"),
        (New-Block 0 1 2 "oak_planks"), (New-Block 4 1 2 "oak_planks")
    ),
    @(
        (New-Block 0 0 1 "sandstone"), (New-Block 0 0 3 "sandstone"),
        (New-Block 1 0 0 "cobblestone"), (New-Block 1 0 1 "gravel"), (New-Block 1 0 2 "suspicious_sand" $shorelineCommon), (New-Block 1 0 3 "gravel"), (New-Block 1 0 4 "cobblestone"),
        (New-Block 2 0 0 "mossy_cobblestone"), (New-Block 2 0 1 "gravel"), (New-Block 2 0 2 "sand"), (New-Block 2 0 3 "suspicious_gravel" $shorelineCommon), (New-Block 2 0 4 "mossy_cobblestone"),
        (New-Block 3 0 0 "cobblestone"), (New-Block 3 0 1 "gravel"), (New-Block 3 0 2 "suspicious_sand" $shorelineRare), (New-Block 3 0 3 "gravel"), (New-Block 3 0 4 "cobblestone"),
        (New-Block 4 0 1 "sandstone"), (New-Block 4 0 3 "sandstone")
    )
)

for ($index = 0; $index -lt $buriedCoinSites.Count; $index++) {
    Write-Site "buried_coin_ruin/site_$($index + 1)" $buriedCoinSites[$index]
}
for ($index = 0; $index -lt $badlandsSites.Count; $index++) {
    Write-Site "badlands_arrowhead_site/site_$($index + 1)" $badlandsSites[$index]
}
for ($index = 0; $index -lt $shorelineSites.Count; $index++) {
    Write-Site "shoreline_relic_cache/site_$($index + 1)" $shorelineSites[$index]
}
