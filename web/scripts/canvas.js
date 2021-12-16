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
        ctx.font = font; //"28px Arial";
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
    // todo make this into a Minecraft pixelated heart
    // heart: function(x, y, cube) {
    //     // DRAW PIXELATED HEART MATCHING STARDEW
    //     // Outside #6a0005
    //     draw.rect(x - (cube * 4), y - (cube * 0), cube * 7, cube * 2, "#6a0005");
    //     draw.rect(x - (cube * 3), y - (cube * 1), cube * 2, cube * 4, "#6a0005");
    //     draw.rect(x + (cube * 0), y - (cube * 1), cube * 2, cube * 4, "#6a0005");
    //     draw.rect(x - (cube * 2), y - (cube * 2), cube * 3, cube * 1, "#6a0005");
    //     draw.rect(x - (cube * 1), y - (cube * 3), cube * 1, cube * 1, "#6a0005");
    //     // Inside #d83a01
    //     draw.rect(x - (cube * 3), y - (cube * 0), cube * 2, cube * 2, "#d83a01");
    //     draw.rect(x - (cube * 0), y - (cube * 0), cube * 2, cube * 2, "#d83a01");
    //     draw.rect(x - (cube * 2), y - (cube * 1), cube * 3, cube * 2, "#d83a01");
    //     draw.rect(x - (cube * 1), y - (cube * 2), cube * 1, cube * 1, "#d83a01");
    //     // Inside highlight #f16f53
    //     draw.rect(x - (cube * 2), y + (cube * 1), cube * 1, cube * 1, "#f16f53");
    //     draw.rect(x + (cube * 1), y + (cube * 1), cube * 1, cube * 1, "#f16f53");

    //     // draw.circle(x, y, 1, "#000000");
    // },
    erase: function() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
    }
};

//******************** Canvas Drawing Functions ********************//

// Variables for drawing bars
var barInterval;
var barPercent = 0;
var barData;
var heightInterval;

function drawBarGraph(data) {
    draw.erase();

    // Set shared variables for the interval
    heightInterval = (canvas.height - 80) / (data.length - 1);
    barPercent = 2;
    barData = data;

    // todo check if first one is 0, then don't continue the interval more than once

    // Start the interval to incrementally draw the bars
    barInterval = setInterval(drawBars, 100);

    // Draw player heads (once for this graph)
    for (let i = 0; i < data.length; i++) {
        draw.image(35, ((i + 1) * heightInterval) - 5, 50, 50, document.getElementById("grassBlock"));
    }
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

    for (let i = 0; i < barData.length; i++) {
        let value = barData[i].value;

        let rectY = canvas.height - ((i + 1) * heightInterval) + 10;

        // Don't draw if no value
        if (value != 0) {
            let rectW = value * scale * percent;

            // Draw bar
            draw.rect(130, rectY, rectW, 30, "limegreen");
            draw.circle(130, rectY + 15, 15, "limegreen");
            draw.circle(130 + rectW, rectY + 15, 15, "limegreen");
        }

        // Draw player name and value
        draw.text(130, rectY + 9, "black", "16px 'Comic Sans MS'", "left", barData[i].player);
        draw.text((canvas.width - 40), rectY + 9, "white", "16px 'Comic Sans MS'", "right", value);
    }

    // Stop the interval when it's filled
    if (barPercent >= 100) {
        clearInterval(barInterval);
    } else {
        barPercent += 4;
    }
}
