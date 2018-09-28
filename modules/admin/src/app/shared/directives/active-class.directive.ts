import { Directive, ElementRef, HostListener, OnInit, Renderer2 } from '@angular/core';

/**
 * @class
 * This defines a directive which can be used in html files, to add an "active" css class to any component when the
 * mouse moves within the bounds of that component, and remove that class when the mouse leaves.
 * Note that the addition of a class re-renders the component, and so if you have many small components on the screen, whose labels/attributes
 * require calling non-trivial functions, those functions could be called very often as the mouse moves around.
 */
@Directive({
  selector: '[appActiveClass]'
})
export class ActiveClassDirective implements OnInit {

  constructor(
    private el: ElementRef,
    private renderer: Renderer2
  ) {  }

  ngOnInit() {
  }

  /**
   * Add the 'active' css class to the component upon mouse entry
   */
  @HostListener('mouseenter') mouseover(eventData: Event) {
    this.renderer.addClass(this.el.nativeElement, 'active');
  }

  /**
   * Remove the 'active' css class to the component upon mouse exit
   */
  @HostListener('mouseleave') mouseleave(eventData: Event) {
    this.renderer.removeClass(this.el.nativeElement, 'active');
  }
}
