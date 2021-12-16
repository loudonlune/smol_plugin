// MC Stats
// Script to help manipulate the html elements on the page

//******************** Defines ********************//

// Define statistics list
var statisticsList = document.getElementById("statisticsList");

// Stuff for typeList
var typeList = document.getElementById("typeList");
var typeListInput = document.getElementById("typeListInput");
var typeListDiv = document.getElementById("typeListDiv");

// Text version of leaderboard
var leaderboardErrorMsg = document.getElementById("leaderboardErrorMsg");
var leaderboardText = document.getElementById("leaderboardText");

//******************** Helper Functions ********************//

// Shows an error
function showError(msg) {
    console.log(msg);
    leaderboardErrorMsg.innerHTML = msg;
}
// todo function to clear this error message

// todo functions to show/hide typeList

// Helper function to quickly add options into a select element
function addOptionToSelect(select, text, value) {
    let opt = document.createElement("option");
    opt.innerHTML = text;
    opt.value = value;
    select.appendChild(opt)
}

// Helper function to fetch and fill the datalist select specifically
async function fillDataList(type) {
    typeListDiv.style.visibility = "visible";
    typeListInput.value = "";
    typeList.innerHTML = "";

    console.log("Must fetch " + type + "!");
    var list = await basicGetFetch(getEndpointFromType(type));

    if (list == null) {
        // Handle if no data is returned
        showError("Error: statisticsList not loaded!");
    } else {
        list.forEach(el => {
            // console.log(el);
            addOptionToSelect(typeList, el, el);
        })
    }
}

// Helper function to display statistic data
function displayStatisticsData(data) {
    // Go right to query
    console.log(data);
    
    if (data == null) {
        // Handle if nothing is returned from get statistic
        showError("Error: getData did not return data!");
    } else {
        // Sort by value (greatest to least)
        data.sort((a, b) => (a.value < b.value) ? 1 : -1);

        // Display in text form
        leaderboardText.innerHTML = "";
        data.forEach(el => {
            leaderboardText.innerHTML += el.value + " - " + el.player;
            leaderboardText.appendChild(document.createElement("br"));
        })

        drawBarGraph(data);
    }
}

//******************** Events ********************//

// On statisticList dropdown selection
statisticsList.oninput = async function() {
    leaderboardText.innerHTML = "";

    let stat = JSON.parse(this.value);
    console.log("Statistic " + stat.statistic + " selected!");

    if (stat.type == "UNTYPED") {
        typeListDiv.style.visibility = "hidden";
        displayStatisticsData(await getUntypedData(stat.statistic));
        
    } else {
        // Need to get type from user as well
        fillDataList(stat.type);
    }
}

// On dataList selection of type param
typeListInput.onblur = async function() {
    leaderboardText.innerHTML = "";

    // todo check text is in datalist

    console.log("Selected " + typeListInput.value + "!");

    let stat = JSON.parse(statisticsList.value);
    displayStatisticsData(await getTypedData(stat.statistic, stat.type, typeListInput.value));
}
// idea add an event that triggers the above on enter key press

//******************** Initial Loading ********************//

// Fetches and loads the statistics on the page
async function loadStatisticsList() {
    var statistics = await basicGetFetch("/getStatisticsList");
    // console.log(statistics);
    
    if (statistics == null) {
        // Handle if something goes wrong (aka no stats)
        showError("Error: No statistics loaded!")
    } else {
        // Load elements into dropdown
        statistics.forEach(el => {
            // console.log(el);
            addOptionToSelect(statisticsList, el.statistic, JSON.stringify(el));
        })
    }

    
}
// After the page first loads
window.addEventListener("load", function() { 
    typeListDiv.style.visibility = "hidden";
    loadStatisticsList();

    // Fix URL based on location
    let tempUrl = window.location.href
    mainUrl = tempUrl.slice(0, tempUrl.lastIndexOf("/")) + "/api/stats";
    console.log("API URL: " + mainUrl);
});
