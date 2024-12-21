import { Renderer2, RendererStyleFlags2 } from '@angular/core';

export class ImageHelper {
    public static loadImage(
        _renderer: Renderer2,
        imageUrl: string,
        imageElement: HTMLImageElement,
        imageBadge: HTMLElement
    ): void {
        ImageHelper.load(imageUrl, imageElement).then(
            (el) => {
                _renderer.setStyle(imageBadge, 'display', 'none', RendererStyleFlags2.Important);
                _renderer.setStyle(el, 'display', '');
            },
            () => {
                _renderer.setStyle(imageElement, 'display', 'none', RendererStyleFlags2.Important);
                _renderer.setStyle(imageBadge, 'display', '');
            }
        );
    }

    private static async load(imageUrl: string, imageElement: HTMLImageElement): Promise<HTMLImageElement> {
        return new Promise((resolve, reject) => {
            imageElement.onload = () => resolve(imageElement);
            imageElement.onerror = reject;
            imageElement.src = imageUrl;
        });
    }
}
