//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

const CACHE_NAME = "{cachename}";

const RESOURCE_FILES = [
    {resourcefiles}
];

self.addEventListener("install", event => {
    console.log("Installing service worker");
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => cache.addAll(RESOURCE_FILES))
            .catch(e => console.log("Failed to cache resource file: " + e))
    );
});

self.addEventListener("fetch", event => {
    event.respondWith(
        caches.match(event.request).then(response => response || fetch(event.request))
    );
});
