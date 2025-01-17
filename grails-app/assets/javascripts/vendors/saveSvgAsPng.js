/**
 CUSTOMIZED !
 The MIT License (MIT)

 Copyright (c) 2014 Eric Shull

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */

(function() {
    var out$ = typeof exports != 'undefined' && exports || this;

    var doctype = '<?xml version="1.0" standalone="no"?><!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">';

    function isExternal(url) {
        return url && url.lastIndexOf('http', 0) == 0 && url.lastIndexOf(window.location.host) == -1;
    }

    function inlineImages(el, callback) {
        var images = el.querySelectorAll('image');
        var left = images.length;
        if (left == 0) {
            callback();
        }
        for (var i = 0; i < images.length; i++) {
            (function(image) {
                var href = image.getAttributeNS("http://www.w3.org/1999/xlink", "href");
                if (href) {
                    if (isExternal(href.value)) {
                        console.warn("Cannot render embedded images linking to external hosts: "+href.value);
                        return;
                    }
                }
                var canvas = document.createElement('canvas');
                var ctx = canvas.getContext('2d');
                var img = new Image();
                href = href || image.getAttribute('href');
                img.src = href;
                img.onload = function() {
                    canvas.width = img.width;
                    canvas.height = img.height;
                    ctx.drawImage(img, 0, 0);
                    image.setAttributeNS("http://www.w3.org/1999/xlink", "href", canvas.toDataURL('image/png'));
                    left--;
                    if (left == 0) {
                        callback();
                    }
                };
                img.onerror = function() {
                    console.log("Could not load "+href);
                    left--;
                    if (left == 0) {
                        callback();
                    }
                }
            })(images[i]);
        }
    }

    function styles(el, selectorRemap) {
        var css = "";
        var sheets = document.styleSheets;
        for (var i = 0; i < sheets.length; i++) {
            if (isExternal(sheets[i].href)) {
                console.warn("Cannot include styles from other hosts: " + sheets[i].href);
                continue;
            }
            var rules = sheets[i].cssRules;
            if (rules != null) {
                for (var j = 0; j < rules.length; j++) {
                    var rule = rules[j];
                    if (typeof(rule.style) != "undefined") {
                        var match = null;
                        try {
                            match = el.querySelector(rule.selectorText);
                        } catch (err) {
                            //console.warn('Invalid CSS selector "' + rule.selectorText + '"', err);
                        }
                        if (match) {
                            var selector = selectorRemap ? selectorRemap(rule.selectorText) : rule.selectorText;
                            css += selector + " { " + rule.style.cssText + " }\n";
                        } else if (rule.cssText.match(/^@font-face/)) {
                            css += rule.cssText + '\n';
                        }
                    }
                }
            }
        }
        return css;
    }

    out$.svgAsDataUri = function(el, options, cb) {
        options = options || {};
        options.scale = options.scale || 1;
        var xmlns = "http://www.w3.org/2000/xmlns/";

        inlineImages(el, function() {
            var outer = document.createElement("div");
            var clone = el.cloneNode(true);
            var width, height, box, svg;
            if (el.tagName == 'svg') {
                box = el.getBoundingClientRect();
                width = parseInt(clone.getAttribute('width') ||
                    box.width ||
                    clone.style.width ||
                    window.getComputedStyle(el).getPropertyValue('width'));
                height = parseInt(clone.getAttribute('height') ||
                    box.height ||
                    clone.style.height ||
                    window.getComputedStyle(el).getPropertyValue('height'));
                if (width === undefined ||
                    width === null ||
                    isNaN(parseFloat(width))) {
                    width = 0;
                }
                if (height === undefined ||
                    height === null ||
                    isNaN(parseFloat(height))) {
                    height = 0;
                }
            } else {
                box = el.getBBox();
                width = box.x + box.width;
                height = box.y + box.height;
                clone.setAttribute('transform', clone.getAttribute('transform').replace(/translate\(.*?\)/, ''));

                svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
                svg.appendChild(clone);
                clone = svg;
            }

            clone.setAttribute("version", "1.1");
            clone.setAttributeNS(xmlns, "xmlns", "http://www.w3.org/2000/svg");
            clone.setAttributeNS(xmlns, "xmlns:xlink", "http://www.w3.org/1999/xlink");
            clone.setAttribute("width", width * options.scale);
            clone.setAttribute("height", height * options.scale);
            clone.setAttribute("viewBox", "0 0 " + width + " " + height);
            outer.appendChild(clone);

            var css = styles(el, options.selectorRemap);
            var s = document.createElement('style');
            s.setAttribute('type', 'text/css');
            s.innerHTML = "<![CDATA[\n" + css + "\n]]>";
            var defs = document.createElement('defs');
            defs.appendChild(s);
            clone.insertBefore(defs, clone.firstChild);

            svg = doctype + outer.innerHTML;
            var uri = 'data:image/svg+xml;base64,' + window.btoa(unescape(encodeURIComponent(svg)));
            if (cb) {
                cb(uri);
            }
        });
    };

    // CUSTOMIZED
    // so it doesn't trigger a.download and use a callback instead
    // and set the background to white
    out$.saveSvgAsPng = function(el, options, callback) {
        options = options || {};
        out$.svgAsDataUri(el, options, function(uri) {
            var image = new Image();
            image.onload = function() {
                var canvas = document.createElement('canvas');
                canvas.width = image.width;
                canvas.height = image.height;
                var context = canvas.getContext('2d');
                context.fillStyle = 'rgba(255,255,255,1)';
                context.fillRect(0, 0, image.width, image.height);
                context.drawImage(image, 0, 0);
                var imageBase64 = canvas.toDataURL('image/png');
                callback(imageBase64);
            };
            image.src = uri;
        });
    }
})();
