# wavelets
Open source DAW. Creativity shouldn't have a price.

Redoing everything with a new engine.
Apache Pivot will be used primarily instead of Swing.
There will (eventually) be full Jython integration.

There's two major roadblocks coming up:
1. Radix-2 DIT DFT (FFT used here) only works for power of 2 sizes. Most numbers aren't powers of 2. Zero padding is an option but it isn't nearly as clean or flexible as using an FFT algorithm which can handle any size. There's the option of dynamically switching like FFTW, but I don't feel like learning all those algorithms. If someone can provide a decent Bluestein implementation I'll be happy.
2. Custom components with Apache Pivot. It probably isn't that hard, but Pivot lacks full tutorials or documentation, so everything is left to us to figure out. So far Pivot has been much easier to work with than Swing, so I'm hoping it won't be that harrowing; the UIs will get more advanced though and so I'll have to move beyond my primitive ways.
