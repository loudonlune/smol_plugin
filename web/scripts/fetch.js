// MC Stats
// Script for fetching data from the MC server

//******************** Defines ********************//

// URL for GET requests
var mainUrl = "http://73.61.100.234:8080/api/stats"; //'http://192.168.93.5:6900/api/stats'
// todo remove these later

//******************** Helper Functions ********************//

// Helper function to fix the mainUrl based off of the location
function fixMainUrl() {
    let tempUrl = window.location.href
    mainUrl = tempUrl.slice(0, tempUrl.lastIndexOf("/")) + "/api/stats";
    console.log("API URL: " + mainUrl);
}

// Helper function to return endpoint based on type
function getEndpointFromType(type) {
    if (type == "BLOCK") {
        return "/getBlockList";
    } else if (type == "ITEM") {
        return "/getItemList";
    } else if (type == "ENTITY") {
        return "/getEntityList";
    } else {
        console.log("Error: type not supported!");
    }
}

// Check for possible errors from the response
async function errorCheckResponse(response) {
    // Error handle in here
    if (response == null) {
        showError("Error: No response received from fetch");
    } else if (response.status == 200) {
        return await response.json();
    } else if (response.status == 404) {
        showError("Error: The requested page endpoint was not found");
    } else if (response.status == 500) {
        showError("Error: an internal server error occurred");
    }
    return null;
}

// // Get response to json format, if it isn't null
// async function (response) {
//     if (response == null) {
//         return null;
//     } else {
//         return await response.json();
//     }
// } // question: is this actually needed?

//******************** Fetch Functions ********************//

// Basic fetch when given a url
async function basicFetch(url) {
    // Clear previous error message
    leaderboardErrorMsg.innerHTML = "";

    console.log("Fetching from: ", url);
    let response = await fetch(url, { method: 'GET' })
        .catch(error => {
            console.log(error); // question redundant?
            showError(error);
            return null;
        })
    
    console.log("\tresponse ", response);

    return errorCheckResponse(response);
}

// Runs a basic fetch to an endpoint without a query
async function basicGetFetch(endpoint) {
    return basicFetch(mainUrl + endpoint);
} // question redundant?

// Gets the statistic given only the statistic and no other queries
async function getUntypedData(stat) {
    return basicFetch(mainUrl + "/getData?" + new URLSearchParams({
        stat: stat, 
    }));
}

// Gets the statistic given a block/item/entity type 
async function getTypedData(stat, type, value) {
    if (type == "BLOCK") {
        return basicFetch(mainUrl + "/getData?" + new URLSearchParams({
            stat: stat, 
            block: value,
        }));
    } else if (type == "ITEM") {
        return basicFetch(mainUrl + "/getData?" + new URLSearchParams({
            stat: stat, 
            item: value,
        }));
    } else if (type == "ENTITY") {
        return basicFetch(mainUrl + "/getData?" + new URLSearchParams({
            stat: stat, 
            entity: value,
        }));
    } else {
        console.log("Error: type not supported!");
    }
}

// Get the head of the given player
// Note: actually doesn't need to be fetched, just set image.src to the url
function getPlayerHead(player) {
    return (mainUrl + "/getPlayerHead?" + new URLSearchParams({
        player: player,
        size: 80,
    }));
}