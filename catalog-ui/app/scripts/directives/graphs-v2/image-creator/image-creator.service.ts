module Sdc.Utils {
    export class ImageCreatorService {
        static '$inject' = ['$q'];
        private _canvas: HTMLCanvasElement;

        constructor(private $q: ng.IQService) {
            this._canvas = <HTMLCanvasElement>$('<canvas>')[0];
            this._canvas.setAttribute('style', 'display:none');

            let body = document.getElementsByTagName('body')[0];
            body.appendChild(this._canvas);
        }

        getImageBase64(imageBaseUri: string, imageLayerUri: string): ng.IPromise<string> {
            let deferred = this.$q.defer();
            let imageBase = new Image();
            let imageLayer = new Image();
            let imagesLoaded = 0;
            let onImageLoaded = () => {
                imagesLoaded++;

                if (imagesLoaded < 2) {
                    return;
                }
                this._canvas.setAttribute('width', imageBase.width.toString());
                this._canvas.setAttribute('height', imageBase.height.toString());

                let canvasCtx = this._canvas.getContext('2d');
                canvasCtx.clearRect(0, 0, this._canvas.width, this._canvas.height);

                canvasCtx.drawImage(imageBase, 0, 0, imageBase.width, imageBase.height);
                canvasCtx.drawImage(imageLayer, imageBase.width - imageLayer.width, 0, imageLayer.width, imageLayer.height);

                let base64Image = this._canvas.toDataURL();
                deferred.resolve(base64Image);
            };

            imageBase.onload = onImageLoaded;
            imageLayer.onload = onImageLoaded;
            imageBase.src = imageBaseUri;
            imageLayer.src = imageLayerUri;

            return deferred.promise;
        }
    }
}