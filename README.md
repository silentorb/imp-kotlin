# Imp

A sImple, purely functional programming language that outputs a node graph

Created by Christopher W. Johnson

Currently Imp is in alpha development

## Motivation

Imp's primary goal is to bridge the gap between art and engineering, particularly for digital content creation such as graphics, sound, and music.

Traditional digital art tools lack the simple power of programming languages, particularly when it comes to  abstraction.  This results in the following challenges:

* Limited support for abstracting patterns and re-using them across projects
* Significant redundancy within large projects
* Destructive workflows that require manually recreating assets for any major changes
* Heavy reliance on resolution-dependent sources

The alternative is to define art in code, which out-of-the-box has several problems of its own:

* The application must be recompiled and/or relaunched to see changes
* Much of art creation involves finding the sweet spot within ranges, which is a tedious process when editing literal code values, especially with non-real-time previews
* A critical feature of most content creation software is muting and soloing aspects of a project, which is not directly supported by coded assets
* Some live-coding languages support manually defining GUIs within the source code to tweak range values, but they have no means of applying those changes back to the source code, and injecting the GUI is both tedious and clutters production code

The Imp programming language and its IntelliJ plugin solve all of the above problems.

### Live Editing

Imp and its tooling is designed to fully support live editing and real time previews.

* Imp is an interpreted language, resulting in minimal compilation latency
* The Imp IntelliJ plugin contains a general framework for live previews, comes packaged with a variety of previews for various data structures (2D images, 3D models, and audio streams), and can be extended to live-preview additional data structures
* If Imp-generated assets are intended to be consumed by a JVM application, Imp is designed to be used as a library and can be easily integrated into a real-time asset generation workflow, allowing live-updates of assets while the target application is running

### Automatic GUI Integration

Imp is designed to seamlessly map source code to GUI form fields.  This is possible because:

* Imp is purely declarative
* Imp is strongly typed
* Imp always tracks type aliases (unlike many strongly typed languages)
* Imp limits its complexity to basic functional abstractions
* Imp outputs a node graph which is easy to reason about and integrate

#### Type-to-Field mapping

One of the challenges of defining a GUI for value ranges is different function parameters will have different acceptable ranges.  For traditional live-coding languages supporting custom GUIs, their GUI functions generally allow the programmer to specify, per numeric literal, what the minimum and maximum values are.

Imp streamlines that process via numeric type constraints.

Comprehensive type constraints has long been an elusive holy grail for programming.  The problem domain of type constraints is so broad that it is hard to create a general solution that is powerful enough to solve even the tip of the ice burg of a programmers potential type constraint needs.  Thus, mainstream programming languages only support the most basic of type constraints.

Imp is somewhat domain specific.  The majority of values that appear in graphics and audio software are numeric, meaning Imp can get a lot of mileage from a single type constraint: numeric min/max ranges.

(TODO: Finish explaining the automatic GUI generation)

## Sample Imp Code

This generates a checkered 2D texture with Perlin noise.

```ocaml
import silentorb.mythic.generation.texturing.*
import silentorb.mythic.generation.drawing.*
import silentorb.mythic.math.*

let background = noise
    scale = 57
    detail = 78
    variation = 1
    . colorize (RgbColor 156 166 227) (RgbColor 0 0 0)

let foreground = noise
    scale = 52
    detail = 41
    variation = 106
    . colorize (RgbColor 2 2 2) (RgbColor 0 0 0)

let output = mask foreground background (checkers 3 3)
    . distort
        scale = 62
        detail = 28
        variation = 18
        strength = 8

```

