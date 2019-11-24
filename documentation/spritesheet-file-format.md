MultimediaLib sprite sheet file format
======================================

MultimediaLib includes a tool for generating sprite sheets. Compared to loading all images 
individually, sprite sheets have better performance characteristics both when parsing the images
and when drawing them.

The command line tool used to generate sprite sheets is described in the section "Packing images 
into a sprite sheet" in the [MultimediaLib documentation](../readme.md). This document contains
an example of the YAML file that conains the sprite sheet metadata.

```
- name: first.png
  x: 0
  y: 0
  width: 64
  height: 64
- name: second.png
  x: 64
  y: 0
  width: 40
  height: 64
```  

The region names are based on the file name of the original images, though the directory path is
removed. The location of the region is indicatd using integer coordinates, that are relative to
the sprite sheet image.
