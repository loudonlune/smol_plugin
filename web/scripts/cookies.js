// MC Stats
// A script to manage cookies for the browser

//******************** Setting a cookie ********************//

// Basic save cookie function, without any checks
function setCookieNoQuestionsAsked(key, data) {
    console.info("Saving cookie named \"" + key + "\"");
    document.cookie = key + "=" + data + "; SameSite=Strict; " + "max-age=" + (14*24*60*60);
}

// Save a cookie with the data given, determines if it needs to be broken up or not
function setCookie(key, data) {
    data = encodeURIComponent(data);
    let size = (new TextEncoder().encode(data)).length + key.length;
    if (size > 4096) {
        console.log("Breaking up large cookie of " + size + " bytes"); // fixme change back to consoleAction after testing
        setBrokenCookie(key, data);
    } else {
        setCookieNoQuestionsAsked(key, data);
    }
}

// Save a cookie in pieces if it's too large to go in one
function setBrokenCookie(key, data) {
    // The main cookie that holds the names of the other cookies (in order)
    let parentCookie = {
        "brokenCookie":[]
    }

    do {
        // Concatenate nth name
        let pieceName = key + (parentCookie.brokenCookie.length);
        parentCookie.brokenCookie.push(pieceName);

        // Break off next chunk of data
        let piece = data.slice(0, (4095 - pieceName.length));
        data = data.slice((4095 - pieceName.length));

        setCookieNoQuestionsAsked(pieceName, piece);
    } while (data.length != 0);

    setCookieNoQuestionsAsked(key, JSON.stringify(parentCookie));
}

//******************** Getting a cookie ********************//
// Just gets the given cookie, doesn't try to piece a broken cookie back together
function getCookieNoQuestionsAsked(key) {
    let cookies = document.cookie.split(";");

    let toReturn = null;
    cookies.some(c => {
        c = c.trim(); // Remove whitespace
        let equalSign = c.indexOf("=");
        let cookieA = keyFromCookieString(c);
        let cookieB = key.normalize();
        if (cookieA === cookieB) {
            toReturn = c.slice(equalSign + 1);
            return true; // Breakout
        }
    });

    return toReturn;
}

// Gets the cookie, determines if it is a broken cookie or not
function getCookieBase(key) {
    let toReturn = getCookieNoQuestionsAsked(key);

    if (toReturn != null && toReturn.indexOf('{"brokenCookie":["') == 0) {
        return getBrokenCookie(toReturn);
    }

    return toReturn;
}

// Gets the cookie but decodes it too
function getCookie(key) {
    let value = getCookieBase(key);
    if (value == null) {
        return null;
    }
    return decodeURIComponent(value);
}

// Pieces together the parts of a broken cookie
function getBrokenCookie(list) {
    let toReturn = "";
    JSON.parse(list).brokenCookie.forEach(c => {
        toReturn += getCookieBase(c);
    });
    return toReturn;
}

//******************** Clear a cookie ********************//

// Deletes the cookie by making it expires
function clearCookie(key) {
    document.cookie = key + "=; SameSite=Strict; expires=Thu, 01 Jan 1970 00:00:00 GMT";
}
// question handle clearing broken cookie pieces too? or just let them expire?

// Function to clear all cookies that are in the document.cookie
function clearAllCookies() {
    let cookies = getCookieKeys();
    cookies.forEach(c => {
        clearCookie(c);
    });
}

//******************** Getting Keys ********************//

// Slices the key from the given "key=value" pair
function keyFromCookieString(c) {
    c = c.trim(); // Remove whitespace
    return c.slice(0, c.indexOf("=")).normalize();
}

// Function to get all keys from the document cookie
function getCookieKeys() {
    let cookies = document.cookie.split(";");

    let toReturn = [];
    cookies.forEach(c => {
        toReturn.push(keyFromCookieString(c));
    });
    return toReturn;
}