const localtunnel = require('localtunnel');
const fs = require('fs');
const path = require('path');

(async () => {
    console.log("Starting live servers...");

    // 1. Start Backend Tunnel
    const backendTunnel = await localtunnel({ port: 5005 });
    console.log(`[Live Backend URL]: ${backendTunnel.url}`);

    // 2. Update Frontend App.jsx to point to the new live backend
    const appJsxPath = path.join(__dirname, '../frontend/src/App.jsx');
    let appJsx = fs.readFileSync(appJsxPath, 'utf8');
    
    // Replace any localhost URL with the new live backend URL
    appJsx = appJsx.replace(/http:\/\/localhost:500[0-9]\/api\/discover/g, `${backendTunnel.url}/api/discover`);
    fs.writeFileSync(appJsxPath, appJsx);
    console.log("[Frontend] Updated App.jsx to use Live Backend URL.");

    // 3. Start Frontend Tunnel
    const frontendTunnel = await localtunnel({ port: 5173 });
    console.log(`[Live Frontend URL]: ${frontendTunnel.url}`);

    console.log("\n========================================================");
    console.log("SUCCESS! Your application is now live on the internet.");
    console.log(`Access the Dashboard here: ${frontendTunnel.url}`);
    console.log("Note: Localtunnel may ask you to click 'Continue' on the first visit.");
    console.log("========================================================\n");

    // Keep process alive
    backendTunnel.on('close', () => {
        console.log("Backend tunnel closed");
    });
    frontendTunnel.on('close', () => {
        console.log("Frontend tunnel closed");
    });

})();
