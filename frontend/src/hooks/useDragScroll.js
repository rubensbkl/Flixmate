// src/hooks/useDragScroll.js

import { useEffect, useRef } from 'react';

export default function useDragScroll() {
  const scrollRef = useRef(null);
  
  useEffect(() => {
    const slider = scrollRef.current;
    if (!slider) return;
    
    let isDown = false;
    let startX;
    let scrollLeft;
    
    const mouseDownHandler = (e) => {
      // Ignorar cliques em elementos dentro do slider (imagens, textos, etc)
      if (e.target !== slider) return;
      
      isDown = true;
      slider.classList.add('active');
      startX = e.pageX - slider.offsetLeft;
      scrollLeft = slider.scrollLeft;
    };
    
    const mouseLeaveHandler = () => {
      isDown = false;
      slider.classList.remove('active');
    };
    
    const mouseUpHandler = () => {
      isDown = false;
      slider.classList.remove('active');
    };
    
    const mouseMoveHandler = (e) => {
      if (!isDown) return;
      e.preventDefault();
      const x = e.pageX - slider.offsetLeft;
      const walk = (x - startX) * 2; // Velocidade do scroll
      slider.scrollLeft = scrollLeft - walk;
    };
    
    // Adicionar event listeners
    slider.addEventListener('mousedown', mouseDownHandler);
    slider.addEventListener('mouseleave', mouseLeaveHandler);
    slider.addEventListener('mouseup', mouseUpHandler);
    slider.addEventListener('mousemove', mouseMoveHandler);
    
    // Limpar event listeners
    return () => {
      slider.removeEventListener('mousedown', mouseDownHandler);
      slider.removeEventListener('mouseleave', mouseLeaveHandler);
      slider.removeEventListener('mouseup', mouseUpHandler);
      slider.removeEventListener('mousemove', mouseMoveHandler);
    };
  }, []);
  
  return scrollRef;
}