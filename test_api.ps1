# Test script for SecureVault API

# Test login endpoint
$loginData = @{
    email = "dharshinimurali63@gmail.com"
    password = "Dharshini3612@"
} | ConvertTo-Json

Write-Host "Testing login endpoint..."
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body $loginData -UseBasicParsing
    Write-Host "Login Success! Status: $($response.StatusCode)"
    
    # Parse the response to get the token
    $responseData = $response.Content | ConvertFrom-Json
    $token = $responseData.data.token
    Write-Host "Token received: $($token.Substring(0,20))..."
    
    # Test credentials endpoint
    Write-Host "Testing credentials endpoint..."
    $credentialData = @{
        serviceName = "TestService"
        username = "testuser@example.com"  
        password = "testpassword123"
        category = "PERSONAL"
    } | ConvertTo-Json
    
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    $credResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/credentials" -Method POST -Body $credentialData -Headers $headers -UseBasicParsing
    Write-Host "Credential Creation Success! Status: $($credResponse.StatusCode)"
    Write-Host "Response: $($credResponse.Content)"
    
} catch {
    Write-Host "Error occurred: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Error content: $errorContent"
    }
}