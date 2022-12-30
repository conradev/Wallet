<script lang="ts">
  export let className: string
  export let text: string
  
  class Point {
    static readonly zero = new Point(0, 0)

    readonly x: number
    readonly y: number

    constructor(x: number, y: number) {
      this.x = x
      this.y = y
    }

    add(other: Point): Point {
      return new Point(this.x + other.x, this.y + other.y)
    }
  }

  class TouchIterable {
    private static Iterator = class implements Iterator<Touch> {
      private index: number = 0
      private readonly touches: TouchList
    
      constructor(touches: TouchList) {
        this.touches = touches
      }
    
      next(...args: [] | [undefined]): IteratorResult<Touch, any> {
        if (this.index < this.touches.length) {
          return {done: false, value: this.touches[this.index++]}
        } else {
          return {done: true, value: undefined}
        }
      }
    }

    private readonly touches: TouchList

    constructor(touches: TouchList) {
      this.touches = touches
    }

    [Symbol.iterator](): Iterator<Touch> {
      return new TouchIterable.Iterator(this.touches)
    }
  }

  function midpoint(touches: TouchList): Point {
    const sum = Array.from(new TouchIterable(touches))
      .map(t => new Point(t.clientX, t.clientY))
      .reduce((p1, p2) => p1.add(p2), Point.zero)
    return new Point( sum.x / touches.length, sum.y / touches.length)
  }

  function contains(rect: DOMRect, point: Point): boolean {
    return (
      point.x >= rect.left &&
      point.x <= rect.right &&
      point.y >= rect.top &&
      point.y <= rect.bottom
    );
  }

  function handler(event: TouchEvent) {
    const target = event.currentTarget
    if (!(target instanceof Element)) {
      return
    }

    const inside = contains(target.getBoundingClientRect(), midpoint(event.targetTouches))
    switch (event.type) {
      case "touchstart":
      case "touchmove": {
        if (inside) {
          target.classList.add("tapped")
        } else {
          target.classList.remove("tapped")
        }
        break
      }
      case "touchend":
      case "touchcancel": {
        target.classList.remove("tapped")
        break
      }
    }
  }

  function close() {
    window.close()
  }
</script>

<style>
  .tappable-button {
    min-width: 45%;
    font-weight: 600;
    font-size: 1.25em;
    border-radius: 15pt;
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    user-select: none;
    -webkit-user-select: none;
    -webkit-tap-highlight-color: transparent;
    transition: filter 0.1s ease-out;
  }
  .tapped {
    filter: brightness(0.8);
  }
</style>

<button class="tappable-button {className}" on:touchstart={handler} on:touchmove={handler} on:touchend={handler} on:touchcancel={handler} on:click={close}>
  <span>{text}</span>
</button>
