# AlphaHearth

[![Build Status](https://travis-ci.org/AlphaHearth/AlphaHearth.svg?branch=master)](https://travis-ci.org/AlphaHearth/AlphaHearth)
[![codecov](https://codecov.io/gh/AlphaHearth/AlphaHearth/branch/master/graph/badge.svg)](https://codecov.io/gh/AlphaHearth/AlphaHearth)

Yes, this project is named after the famous AlphaGo.

As a MCTS AI for Hearthstone, AlphaHearth is still under construction. It uses a greatly changed version of [Brazier](https://github.com/HearthSim/Brazier) as its game simulator, which provides a better API, naming consistency and documentation than the original version. For more information, see the [Brazier](Brazier) module.

## Notice for Followers

First of all, thank you for watching/forking/staring this repository.

Several people have been watching this repository, so I feel obliged to inform you the currect status of this project. The last time I commit is couple of months ago, but you can be sure that the project is not abandoned. The core algorithm (MCS, MCTS) of AlphaHearth is already completed, but as an advanced AI, AlphaHearth should do better than using brute-force searching to predict opponent's action. To predict heuristically, data of all popular decks must be gathered, and that is why I am now dedicating to my own Hearthstone database website, [HearthIntellect](https://github.com/Mr-Dai/HearthIntellect). But as a just-graduted student, my skill is really limited, and to finish such a website, I have to finish tons of tutorials and developer guides first, including HTML5, CSS3, jQuery, Bootstrap, Spring, Mybatis, etc.

As my graduation dissertation, AlphaHearth has already won an A for me. And being a big fan of game AI and Hearthstone, I might never abandon AlphaHearth and HearthIntellect, even if they might not seem to be under active development currently.

# License

AlphaHearth, a MCTS AI for Hearthstone: Heroes of Warcraft.
Copyright (C) 2016  Robert Peng

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
[GNU General Public License](LICENSE) for more details.
