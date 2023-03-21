import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import useEventBus from "../hooks/useEventBus";

const Main = () => {
	const { eventbus, affine } = useEventBus();
	const [ image, setImage ] = useState(`http://${process.env.REACT_APP_IP}:8393/selected`);
	const canvasRef = useRef(null);
	const navigate = useNavigate();

	useEffect(() => {
		const img = new Image();
		const canvas = canvasRef.current;
		const ctx = canvas.getContext('2d');

		const handlerLoad = () => {
			fetch(`http://${process.env.REACT_APP_IP}:8393/selected`)
				.then(value => value.blob())
				.then(value => URL.createObjectURL(value))
				.then(value => setImage(value));
		}

		img.onload = () => {
			canvas.width = canvas.parentElement.offsetWidth;
			canvas.height = canvas.parentElement.offsetHeight;

			const scale = Math.min(canvas.width / img.width, canvas.height / img.height) * .81;

			ctx.setTransform(1, 0, 0, 1, canvas.width / 2, canvas.height / 2);
			ctx.translate(affine.kx, affine.ky)
			ctx.rotate(affine.a);
			ctx.scale(affine.sx * scale, affine.sy * scale);
			ctx.drawImage(img, - img.width / 2, -img.height / 2, img.width, img.height);
		};

		img.src = image;

		window.addEventListener('resize', img.onload);
		eventbus.registerHandler('buffer.update.image', handlerLoad);
		return () => {
			window.removeEventListener('resize', img.onload);
			eventbus.unregisterHandler('buffer.update.image', handlerLoad);
		}
	}, [image, affine, eventbus]);

	return (
		<div className="w-full h-[calc(100%-170px)]" onClick={() => navigate('/main')}>
			<canvas className="w-full h-full bg-black" ref={canvasRef} />
		</div>
	);
};

export default Main;