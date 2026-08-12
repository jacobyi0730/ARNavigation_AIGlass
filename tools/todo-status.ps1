[CmdletBinding()]
param(
    [ValidateRange(-1, 14)]
    [int]$Stage = -1,

    [ValidatePattern('^(S\d{2}-T\d{3}|DOD-\d{2}|BL-\d{2}|COORD-\d{2})$')]
    [string]$TaskId,

    [switch]$PendingOnly
)

$workspaceRoot = Split-Path -Parent $PSScriptRoot
$tasksRoot = Join-Path $workspaceRoot 'docs\tasks'
$taskPattern = '^- \[(?<mark>[ xX])\] \[(?<id>S\d{2}-T\d{3}|DOD-\d{2}|BL-\d{2}|COORD-\d{2})\] (?<title>.+)$'

$taskFiles = Get-ChildItem -File -LiteralPath $tasksRoot -Filter '*.md' | Sort-Object Name
$tasks = foreach ($file in $taskFiles) {
    foreach ($line in Get-Content -Encoding UTF8 -LiteralPath $file.FullName) {
        if ($line -match $taskPattern) {
            $taskMark = $matches.mark
            $currentTaskId = $matches.id
            $taskTitle = $matches.title
            $stageNumber = if ($currentTaskId -match '^S(?<stage>\d{2})-') {
                [int]$matches.stage
            } else {
                $null
            }

            [PSCustomObject]@{
                Id = $currentTaskId
                Stage = $stageNumber
                Status = if ($taskMark -match '[xX]') { 'done' } else { 'pending' }
                Title = $taskTitle
                File = $file.FullName.Substring($workspaceRoot.Length + 1)
            }
        }
    }
}

if ($TaskId) {
    $matchedTask = $tasks | Where-Object Id -EQ $TaskId
    if (-not $matchedTask) {
        Write-Error "Task ID not found: $TaskId"
        exit 1
    }
    $matchedTask
    exit 0
}

if ($Stage -ge 0) {
    $stageTasks = $tasks | Where-Object Stage -EQ $Stage
    if ($PendingOnly) {
        $stageTasks = $stageTasks | Where-Object Status -EQ 'pending'
    }
    $stageTasks
    exit 0
}

$tasks |
    Where-Object { $null -ne $_.Stage } |
    Group-Object Stage |
    Sort-Object { [int]$_.Name } |
    ForEach-Object {
        $done = @($_.Group | Where-Object Status -EQ 'done').Count
        $total = $_.Count
        [PSCustomObject]@{
            Stage = [int]$_.Name
            Done = $done
            Pending = $total - $done
            Total = $total
            Progress = if ($total -eq 0) { '0%' } else { '{0:P0}' -f ($done / $total) }
        }
    }
