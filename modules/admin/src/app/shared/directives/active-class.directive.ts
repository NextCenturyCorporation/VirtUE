import { Directive, ElementRef, HostListener, OnInit, Renderer2 } from '@angular/core';

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

  @HostListener('mouseenter') mouseover(eventData: Event) {
    this.renderer.addClass(this.el.nativeElement, 'active');
  }
  @HostListener('mouseleave') mouseleave(eventData: Event) {
    this.renderer.removeClass(this.el.nativeElement, 'active');
  }
}
