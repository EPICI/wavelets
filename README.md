# wavelets
Open source DAW. Creative expression shouldn't have a price.

Migrated to GitLab on 2018-06-11.

Right now it's far from finished, so this message here is for anyone that might want to contribute rather than the future users. The development team is small (one person actually) so progress is slow. It's important to not push goals ahead, or it'll never be done - v1 was sort of a test run, v2 (the current) is targeting a usable state, a possible v3 would compete with other programs.

Coming much later than Blender and other open source projects, we learn some things - we want flexibility, but not at the cost of ease of use. We should expose a simple and intuitive UI, including more advanced options only when justified. For example, bezier splines will be good enough for most use cases, and if you need more you should make it yourself.

The notions of OOP, nodes, and hooks go well together. Optimally we'd have total freedom and could plug anything into anything else (and somehow retain performance), but it makes it a nightmare to program and the UI gets clunky.

Naming and Implementation
---
The music industry today still resembles analog. Heck, DAWs have knobs and sliders and stuff which is made to look like old fashioned tech. Packaging and selling of music hasn't changed other than the stores becoming digital.

There's two big things you'll want to criticize me on: bad naming and bad implementations. A third bonus is bad design.

For bad implementation, it is partially my fault. However, understand that the companies want to keep their methods (algorithms etc.) a secret, which means I have to rediscover the science of audio engineering and all by myself. Excuse me if the methods I choose are bad.

For bad naming, my philosophy is that we shouldn't be chained so tightly by history and a need for backward compatibility, and we should be able to make new designs and pick new names without worry of conflict with existing ones. So my names are not so standard, and might not describe things which exist in classic music literature anyway, deal with it, or go along with it. If it's exceptionally bad though, we can always give it a different name on the outside (UI) and keep the strange name internally (code).

As for bad design, it's partially because my areas of expertise don't help me design those bits well, and partially because I'm unfamiliar with what others have done. However, if I had one of those $XXX super expensive DAWs I probably wouldn't be making Wavelets. Some individual design choices are weird because I made them, then implemented them after a long hiatus after which I'd have forgotten what that design choice was for, and mis-implemented my own thing.
