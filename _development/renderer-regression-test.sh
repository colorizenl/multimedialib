#!/bin/bash

set -e

mkdir -p build/renderer-regression-test

# Render screenshots for all renderers

gradle launchDemoApplication \
  --args='--renderer java2d --demo regression --screenshot build/renderer-regression-test/desktop-java2d.png'

gradle launchDemoApplication \
  --args='--renderer gdx --demo regression --screenshot build/renderer-regression-test/desktop-gdx.png'

gradle transpileDemoApplication
jwebserver -d build/browserdemo -p 7788 &
JWEBSERVER_PID=$!
trap 'kill JWEBSERVER_PID 2>/dev/null' EXIT

browser_screenshot.py \
  --url "http://localhost:7788/?demo=regression&renderer=canvas" \
  --width 503 \
  --height 1311 \
  --out build/renderer-regression-test/browser-canvas.png

browser_screenshot.py \
  --url "http://localhost:7788/?demo=regression&renderer=gdx" \
  --width 503 \
  --height 1311 \
  --out build/renderer-regression-test/browser-gdx.png

# Compare screenshots against baseline

echo "Comparing results for Java2D renderer"

compare_screenshots.py \
  _development/renderer-regression-test-baseline/desktop-java2d.png \
  build/renderer-regression-test/desktop-java2d.png

echo "Comparing results for libGDX desktop renderer"

compare_screenshots.py \
  _development/renderer-regression-test-baseline/desktop-gdx.png \
  build/renderer-regression-test/desktop-gdx.png

echo "Comparing results for HTML canvas renderer"

compare_screenshots.py \
  _development/renderer-regression-test-baseline/browser-canvas.png \
  build/renderer-regression-test/browser-canvas.png

echo "Comparing results for libGDX browser renderer"

compare_screenshots.py \
  _development/renderer-regression-test-baseline/browser-gdx.png \
  build/renderer-regression-test/browser-gdx.png

echo "Renderer visual regression test OK"
