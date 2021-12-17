// MC Stats
// Script for managing and drawing on the canvas

//******************** Canvas Setup ********************//
const canvas = document.getElementById("myCanvas");
var ctx = canvas.getContext("2d");
ctx.lineWidth = 1;

// Set origin to bottom corner, like a normal graph
ctx.translate(0, canvas.height);
ctx.scale(1, -1);

//******************** CTX Drawing Functions ********************//
const draw = {
    circle: function(x, y, radius, color) {
        ctx.beginPath();
        ctx.arc(x, y, radius, 0, 2 * Math.PI);
        // ctx.strokeStyle = color;
        // ctx.stroke();
        ctx.fillStyle = color;
        ctx.fill();
    },
    line: function(x1, y1, x2, y2, strokeWidth, color) {
        let tempStroke = ctx.lineWidth;
        ctx.lineWidth = strokeWidth;
        ctx.strokeStyle = color;
        ctx.beginPath();
        ctx.moveTo(x1, y1);
        ctx.lineTo(x2, y2);
        ctx.stroke();
        ctx.lineWidth = tempStroke;
    },
    rect: function(x, y, width, height, color) {
        ctx.beginPath();
        ctx.rect(x, y, width, height);
        // ctx.strokeStyle = color;
        // ctx.stroke();
        ctx.fillStyle = color;
        ctx.fill();
    },
    text: function(x, y, color, font, textAlign, text) {
        ctx.font = font;
        ctx.textAlign = textAlign;
        ctx.fillStyle = color;
        ctx.scale(1, -1);
        ctx.fillText(text, x, y * -1);
        ctx.scale(1, -1);
    },
    image: function(x, y, width, height, image) {
        ctx.scale(1, -1);
        ctx.drawImage(image, x, y * -1, width, height);
        ctx.scale(1, -1);
    },
    heart: function(xOrig, yOrig, cube) {
        // DRAW PIXELATED HEART MATCHING MINECRAFT
        x = xOrig - (cube * 0.5);
        y = yOrig - (cube * 1);

        // Outside #000000
        let outside = "#000000";
        draw.rect(x - (cube * 4), y + (cube * 0), cube * 9, cube * 3, outside);
        draw.rect(x - (cube * 3), y - (cube * 1), cube * 7, cube * 5, outside);
        draw.rect(x - (cube * 2), y - (cube * 2), cube * 2, cube * 7, outside);
        draw.rect(x + (cube * 1), y - (cube * 2), cube * 2, cube * 7, outside);
        draw.rect(x - (cube * 1), y - (cube * 3), cube * 3, cube * 2, outside);
        draw.rect(x - (cube * 0), y - (cube * 4), cube * 1, cube * 1, outside);

        // Inside #f34135
        let inside = "#f34135";
        draw.rect(x - (cube * 3), y + (cube * 0), cube * 7, cube * 3, inside);
        draw.rect(x - (cube * 2), y - (cube * 0), cube * 2, cube * 4, inside);
        draw.rect(x + (cube * 1), y - (cube * 0), cube * 2, cube * 4, inside);
        draw.rect(x - (cube * 2), y - (cube * 1), cube * 5, cube * 1, inside);
        draw.rect(x - (cube * 1), y - (cube * 2), cube * 3, cube * 2, inside);
        
        // Bottom #be340c
        let bottom = "#be340c";
        draw.rect(x - (cube * 3), y - (cube * 0), cube * 1, cube * 1, bottom);
        draw.rect(x + (cube * 3), y - (cube * 0), cube * 1, cube * 1, bottom);
        draw.rect(x - (cube * 2), y - (cube * 1), cube * 1, cube * 1, bottom);
        draw.rect(x + (cube * 2), y - (cube * 1), cube * 1, cube * 1, bottom);
        draw.rect(x - (cube * 1), y - (cube * 2), cube * 1, cube * 1, bottom);
        draw.rect(x + (cube * 1), y - (cube * 2), cube * 1, cube * 1, bottom);
        draw.rect(x - (cube * 0), y - (cube * 3), cube * 1, cube * 1, bottom);
        
        // Detail #fdfffa
        draw.rect(x - (cube * 2), y + (cube * 2), cube * 1, cube * 1, "#fdfffa");

        // draw.circle(xOrig, yOrig, 2, "blue");
    },
    erase: function() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
    }
};

//******************** Canvas Drawing Functions ********************//

// Variables for drawing bars
var barInterval;
var barPercent = 0;
var barTotal;
var barData;
var heightInterval;

// todo add checkbox for comic sans vs minecraft font

function drawBarGraph(data) {
    draw.erase();

    // Set shared variables for the interval
    barPercent = 2;
    barTotal = data.length;
    if (barTotal > 5) {
        barTotal = 5;
    }
    heightInterval = 110;
    barData = data;

    // Check if first one is 0, then don't continue the interval more than once
    if (data[0].value == 0) {
        barPercent = 100;
    }

    // Start the interval to incrementally draw the bars
    barInterval = setInterval(drawBars, 100);

    // Draw player heads (once for this graph)
    for (let i = 0; i < barTotal; i++) {
        let img = findPlayerHead(barData[i].player);
        // Make sure image has loaded
        if (img.complete) {
            draw.image(35, canvas.height - ((i) * heightInterval) - 35, 50, 50, img);
        } else {
            // Temp loading image of grass
            draw.image(35, canvas.height - ((i) * heightInterval) - 35, 50, 50, document.getElementById("grassBlock"));
            // Draw the image when it's finished loading
            img.onload = async function() {
                draw.image(35, canvas.height - ((i) * heightInterval) - 35, 50, 50, img);
                if (i == 0) {
                    draw.heart(35, canvas.height - 35, 4);
                }
            }
        }
    }

    // Top player gets a heart
    draw.heart(35, canvas.height - 35, 4);
}

// Interval function to draw bars based off of saved data and a percents
function drawBars() {
    console.log("Drawing bars");

    // Erase previous bars area first
    ctx.clearRect(114, 0, canvas.width, canvas.height);

    // Scale so that the largest value fills the screen
    let scale = (canvas.width - 175) / barData[0].value;

    // Scale so the bar grows over the interval time
    let percent = barPercent / 100;

    for (let i = 0; i < barTotal; i++) {
        let value = barData[i].value;

        let rectY = canvas.height - ((i) * heightInterval) - 40 - 35;

        // Don't draw if no value
        if (value != 0) {
            let rectW = value * scale * percent;

            // Draw bar
            draw.rect(130, rectY, rectW, 30, "limegreen");
            draw.circle(130, rectY + 15, 15, "limegreen");
            draw.circle(130 + rectW, rectY + 15, 15, "limegreen");
        }

        // Draw player name and value
        draw.text(130, rectY + 8, "black", "18px Minecraft", "left", barData[i].player);
        draw.text((canvas.width - 40), rectY + 8, "white", "18px Minecraft", "right", value);
    }

    // Stop the interval when it's filled
    if (barPercent >= 100) {
        clearInterval(barInterval);
    } else {
        barPercent += 4;
    }
}
