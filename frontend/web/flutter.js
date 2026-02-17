// Copyright 2014 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

/**
 * This script installs the service worker and initializes the Flutter engine.
 *
 * It is used by the index.html file to load the application.
 */
(function() {
  "use strict";

  var _flutter = {};
  if (typeof window !== "undefined") {
    window._flutter = _flutter;
  }

  /**
   * The public interface for the Flutter loader.
   */
  _flutter.loader = {
    /**
     * Loads the main entrypoint of the application.
     *
     * @param {Object} options Options for loading the entrypoint.
     * @param {Object} options.serviceWorker Options for the service worker.
     * @param {string} options.serviceWorker.serviceWorkerVersion The version of the service worker.
     * @param {number} options.serviceWorker.timeoutMillis The timeout for the service worker.
     * @param {string} options.serviceWorker.url The URL of the service worker.
     * @param {Function} options.onEntrypointLoaded Callback when the entrypoint is loaded.
     * @returns {Promise} A promise that resolves when the entrypoint is loaded.
     */
    loadEntrypoint: function(options) {
      var serviceWorker = options.serviceWorker || {};
      var serviceWorkerVersion = serviceWorker.serviceWorkerVersion;
      var timeoutMillis = serviceWorker.timeoutMillis || 4000;
      var url = serviceWorker.url || "flutter_service_worker.js?v=" + serviceWorkerVersion;
      var onEntrypointLoaded = options.onEntrypointLoaded;

      return new Promise(function(resolve, reject) {
        // Check if the browser supports service workers.
        if ("serviceWorker" in navigator) {
          navigator.serviceWorker.register(url)
            .then(function(reg) {
              if (!reg.active && (reg.installing || reg.waiting)) {
                // No active web worker and we have installed or are installing
                // one for the first time. Simply wait for it to activate.
                var sw = reg.installing || reg.waiting;
                var listen = function() {
                  if (sw.state === "activated") {
                    sw.removeEventListener("statechange", listen);
                    resolve(reg);
                  }
                };
                sw.addEventListener("statechange", listen);
              } else if (!reg.active.scriptURL.endsWith(serviceWorkerVersion)) {
                // When the app updates the serviceWorkerVersion changes, so we
                // need to ask the service worker to update.
                console.log("New service worker available.");
                reg.update().then(function() {
                  resolve(reg);
                });
              } else {
                // Existing service worker is up-to-date.
                resolve(reg);
              }
            })
            .catch(function(error) {
              console.warn("Service worker registration failed:", error);
              resolve(null);
            });
          
          // Timeout race condition
          setTimeout(function() {
             if (!serviceWorkerVersion) {
               resolve(null);
             }
          }, timeoutMillis);
        } else {
          // Service workers not supported.
          resolve(null);
        }
      }).then(function(reg) {
        // Load main.dart.js
        var script = document.createElement("script");
        script.src = "main.dart.js";
        script.type = "application/javascript";
        document.body.append(script);
        
        if (onEntrypointLoaded) {
            // Mocking engine initializer for simplicity in this setup
            // In a real build, this is provided by main.dart.js
             var engineInitializer = {
                initializeEngine: function() {
                    return Promise.resolve({
                        runApp: function() {
                            // App run logic is handled by main.dart.js
                            console.log("App running...");
                        }
                    });
                }
            };
            // Wait for main.dart.js to define _flutter.loader.didCreateEngineInitializer
            // This is a simplified version.
             onEntrypointLoaded(engineInitializer);
        }
      });
    }
  };
})();
