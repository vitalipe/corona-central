goog.provide("corona.ui.draw.demo")
goog.require("corona.sim.py.sim")




let {next_level, construct} = corona.sim.py.sim




let population_size = 200;

function draw(ctx) {
  if (!ctx) return // FIXME: called with undef first time

  ctx.background(150, 0, 200);
  ctx.circle(110, 110 , population_size)
}


// export
corona.ui.draw.demo.draw = draw;
