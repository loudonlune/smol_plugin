// MC Stats
// A script to manage cookies for the browser

//******************** Setting a cookie ********************//

// Saves a cookie with the key name and the data given
function setCookie(key, data) {
    console.log("Setting cookie " + key + " to " + data) //fixme
    document.cookie = key + "=" + data + "; SameSite=Strict; " + "max-age=" + (30*24*60*60);
}

//******************** Getting a cookie ********************//

// Goes through all of the cookies saved in the document and returns the one with the same key
function getCookie(key) {
    // console.log("\tlooking for " + key) // fixme
    let cookies = document.cookie.split(";");

    let toReturn = null;
    cookies.every(c => { // todo change to .some()?
        c = c.trim(); // Remove whitespace
        let equalSign = c.indexOf("=");
        let cookieA = c.slice(0, equalSign).normalize();
        let cookieB = key.normalize();
        if (cookieA === cookieB) {
            toReturn = c.slice(equalSign + 1);
            return false; // Break but for .every()
        }
        return true; // Strange logic to keep .every() going
    })
    return toReturn;
}

// todo
// idea getBrokenCookie: gets data from multiple cookies and puts it back together into one string
// key=[key1, key2, key3]: data for the key is split between 3 other cookies and the cookie for key has the names of those cookies

//******************** Clear a cookie ********************//

// Deletes the cookie by making it expires
function clearCookie(key) {
    document.cookie = key + "=; SameSite=Strict; expires=Thu, 01 Jan 1970 00:00:00 GMT";
}

//******************** Getting All Keys ********************//
// todo function to get all keys from the cookie


//******************** Light Compression ********************//
// Keys format ex: [['"statistic":', '"s":'], ['"type":', '"t":'], ['"UNTYPED"', '"u"']]

function compressForCookie(string, keys) {
    // console.log(string);
    // console.log((new TextEncoder().encode(string)).length);

    keys.forEach(k => {
        string = string.replace(new RegExp(k[0], 'g'), k[1]);
    });
    
    // console.log(string);
    // console.log((new TextEncoder().encode(string)).length);

    return string;
}

function decompressForCookie(string, keys) {
    keys.forEach(k => {
        string = string.replace(new RegExp(k[1], 'g'), k[0]);
    });
    return string;
}

//******************** Testing ********************//

// setCookie("testkey1", "test 1 data here");
// setCookie("testkey2", "test2 electric bugaloo");

// clearCookie("testkey1");

// console.log("cookie", document.cookie, document.cookie.split(";"));

// console.log(getCookie("testkey2"));
// console.log(getCookie("testkey2nonexistant"));

