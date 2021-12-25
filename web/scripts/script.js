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
var leaderboardTable = document.getElementById("leaderboardTable");

// The page
var page = document.getElementById("page");

// The invisible image div for saving images
var imageDiv = document.getElementById("imageDiv");

//******************** Helper Functions ********************//

// Shows an error
function showError(msg) {
    console.log(msg);
    leaderboardErrorMsg.innerHTML = msg;
}
// todo function to clear this error message?

// Functions to show/hide typeList
function hideTypeList() {
    console.log("(Action) Hiding typelist");
    typeListDiv.style.visibility = "hidden";
    typeListDiv.style.width = 0;
}
function showTypeList() {
    console.log("(Action) Showing typelist)");
    typeListDiv.style.visibility = "visible";
    typeListDiv.style.width = 80 * 4 + "px";
}

// Helper function to quickly add options into a select element
function addOptionToSelect(select, text, value) {
    let opt = document.createElement("option");
    opt.innerHTML = text;
    opt.value = value;
    select.appendChild(opt)
}

// Helper function to fetch and fill the datalist select specifically
async function fillDataList(type) {
    showTypeList();
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

// Function to make the table for a text-version of the leaderboard
function buildLeaderboardTable(data) {
    // Clear table
    leaderboardTable.innerHTML = "";

    // Build each row on the table
    data.forEach(el => {
        let tr = leaderboardTable.insertRow(leaderboardTable.rows.length);

        let head = tr.insertCell(0); 
        let img = document.createElement("img");
        img.src = (findPlayerHead(el.player)).src;
        img.style.width = "40px";
        img.style.height = "40px";
        head.appendChild(img);

        let name = tr.insertCell(1); 
        name.innerHTML = el.player;

        let value = tr.insertCell(2); 
        value.innerHTML = el.value;
    })
}

// Helper function to display statistic data
function displayStatisticsData(data) {
    // console.log(data);

    if (data == null) {
        // Handle if nothing is returned from get statistic
        showError("Error: getData did not return data!");
    } else {
        // Sort by value (greatest to least)
        data.sort((a, b) => (a.value < b.value) ? 1 : -1);

        // Display in table
        buildLeaderboardTable(data);

        drawBarGraph(data);
    }
}

// Helper function to see if the given player head was cached in the imageDiv
// Images must be in the html to be drawn on the canvas
function findPlayerHead(player) {
    let playerHead = imageDiv.children[player];
    // console.log(playerHead);

    // Crete image if not cached in the imageDiv
    if (playerHead == null) {
        playerHead = document.createElement("img");
        playerHead.id = player;
        playerHead.src = getPlayerHead(player);
        playerHead.width = 0;
        playerHead.height = 0;
        imageDiv.appendChild(playerHead);
    }
    // console.log(playerHead.complete)
    // console.log(playerHead);

    return playerHead;
}

//******************** Events ********************//

// On statisticList dropdown selection
statisticsList.oninput = async function() {
    let stat = JSON.parse(this.value);
    console.log("Statistic " + stat.statistic + " selected!");

    if (stat.type == "UNTYPED") {
        hideTypeList(); 
        displayStatisticsData(await getUntypedData(stat.statistic));
        
    } else {
        // Need to get type from user as well
        fillDataList(stat.type);
    }
}

// On dataList selection of type param
typeListInput.onblur = async function() {
    // Make sure something was inputted, else stop
    if (typeListInput.value == "") {
        return 0;
    }

    // Check that the input is a valid selection from the datalist
    let optionFound = false;
    for (let i = 0; i < typeList.options.length; i++) {
        if (typeList.options[i].innerHTML == typeListInput.value) {
            optionFound = true;
            break;
        }
    }
    if (!optionFound) {
        return 0;
    }

    console.log("Selected " + typeListInput.value + "!");

    let stat = JSON.parse(statisticsList.value);
    displayStatisticsData(await getTypedData(stat.statistic, stat.type, typeListInput.value));
}
// idea add an event that triggers the above on enter key press

//******************** Initial Loading ********************//

// Fetches and loads the statistics on the page
async function loadStatisticsList() {
    var statistics = getCookie("statisticsList");
    if (statistics != null) {
        console.log("(Action) Got statisticsList from cookie!");
        statistics = JSON.parse(decompressForCookie(statistics, [['"statistic":', '"s":'], ['"type":', '"t":'], ['"UNTYPED"', '"u"']]));
    } else {
        statistics = await basicGetFetch("/getStatisticsList");

        if (statistics == null) {
            showError("Error: No statistics loaded!");
            return;
        } else {
            let s = JSON.stringify(statistics);

            // Compress manually to get under 4kb for cookie
            s = compressForCookie(s, [['"statistic":', '"s":'], ['"type":', '"t":'], ['"UNTYPED"', '"u"']]);
            setCookie("statisticsList", s);
        }
    }
    // console.log(statistics);

    // Sort by value (alphabetized)
    statistics.sort((a, b) => (a.statistic > b.statistic) ? 1 : -1);

    // Load elements into dropdown
    statistics.forEach(el => {
        // console.log(el);
        addOptionToSelect(statisticsList, el.statistic, JSON.stringify(el));
    })
}
// After the page first loads
window.addEventListener("load", function() { 
    hideTypeList();

    // fixMainUrl(); // fixme REMEMBER TO UNCOMMENT

    // todo add endpoint for mc version 
    // todo check mc version and then invalidate saved cookies if changed so everything will get fetched again

    loadStatisticsList();
});