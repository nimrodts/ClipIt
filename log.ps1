$package = "com.nimroddayan.couponmanager"

Write-Host "------------------------------------------------" -ForegroundColor Cyan
Write-Host "  Auto-Logcat Monitor for $package" -ForegroundColor Cyan
Write-Host "------------------------------------------------" -ForegroundColor Cyan

while ($true) {
    # 1. Wait for App to Start
    $pidStr = ""
    Write-Host -NoNewline "Waiting for app to start... "
    while ([string]::IsNullOrWhiteSpace($pidStr)) {
        try {
            $pidStr = (adb shell pidof -s $package).Trim()
        } catch {
            # Ignore errors (e.g. device disconnected)
        }
        if ([string]::IsNullOrWhiteSpace($pidStr)) { 
            Start-Sleep -Seconds 1 
        }
    }
    
    Write-Host "`n[Connected] App found (PID: $pidStr). Streaming logs..." -ForegroundColor Green
    
    # 2. Start Logcat in background job/process sharing the same window
    # We use Start-Process to allow us to kill it easily later
    $pinfo = New-Object System.Diagnostics.ProcessStartInfo
    $pinfo.FileName = "adb.exe"
    $pinfo.Arguments = "logcat -v color --pid=$pidStr"
    $pinfo.UseShellExecute = $false
    $pinfo.RedirectStandardOutput = $false # Share console output directly
    $pinfo.RedirectStandardError = $false
    
    $process = [System.Diagnostics.Process]::Start($pinfo)

    # 3. Monitor Loop: Check if app died/restarted
    while (-not $process.HasExited) {
        Start-Sleep -Seconds 1
        
        $currentPid = ""
        try {
            $currentPid = (adb shell pidof -s $package).Trim()
        } catch {
            $currentPid = ""
        }

        # If PID changed (empty or different), the app died
        if ($currentPid -ne $pidStr) {
            Write-Host "`n[Detected] App stopping or restarting (Old PID: $pidStr)..." -ForegroundColor Yellow
            
            # Kill the logcat process since it's now stale
            if (-not $process.HasExited) {
                $process.Kill()
            }
            break # Break inner loop, go back to top
        }
    }
    
    # Clean up just in case
    if (-not $process.HasExited) { $process.Kill() }
    Write-Host "Session ended." -ForegroundColor DarkGray
}
