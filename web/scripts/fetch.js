// MC Stats
// Script for fetching data from the MC server

//******************** Defines ********************//

// URL for GET requests
var mainUrl = 'http://192.168.93.5:6900/api/stats'
// todo add grab own url
console.log("url: " + window.location.href);

//******************** Helper Functions ********************//

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

//******************** Fetch Functions ********************//

// Basic fetch when given a url
async function basicFetch(url) {
    // Clear previous error message
    leaderboardErrorMsg.innerHTML = "";

    console.log("Fetching from: ", url);
    let response = await fetch(url, { method: 'GET' })
        .catch(error => {
            console.log(error);
            showError(error);
            return null;
        })
    
    console.log("\tresponse ", response);

    // Error handle in here
    if (response == null) {
        showError("Error: No response received from fetch");
        return null;
    } else if (response.status == 200) {
        return await response.json();
    } else if (response.status == 404) {
        showError("Error: The requested page endpoint was not found");
        return null;
    } else if (response.status == 500) {
        showError("Error: an internal server error occurred");
        return null;
    } else {
        return null;
    }
}

// Runs a basic fetch to an endpoint without a query
async function basicGetFetch(endpoint) {
    return basicFetch(mainUrl + endpoint);
}

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
