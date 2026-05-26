$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$outDir = Join-Path $root "out"
$sourceDir = Join-Path $root "src"
$javafxLib = Join-Path $root "lib\javafx"

New-Item -ItemType Directory -Force -Path $outDir | Out-Null

$sources = Get-ChildItem $sourceDir -Recurse -Filter *.java | ForEach-Object { $_.FullName }

javac --module-path $javafxLib --add-modules javafx.controls,javafx.fxml -d $outDir $sources
java --module-path "$javafxLib;$outDir" --module DistributedVotingSystem/com.voting.server.VotingServer
