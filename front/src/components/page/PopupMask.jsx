import { useEffect, useState } from "react";
import useEventBus from "../../hooks/useEventBus";

const PopupMask = () => {
	const { buffer, eventbus } = useEventBus();
	const { currentMask, mask } = buffer;
	const current = mask[currentMask];

	return (
		<div className="min-w-[240px] pt-2 px-2">
			<h2 className="text-center font-bold text-xl mb-4">Маски для зображення</h2>
			<select className="border border-black/60 bg-black/60 mb-2 p-2 rounded-md cursor-pointer text-white" onChange={e => eventbus.publish('buffer.transformation', e.target.value)} defaultValue={currentMask}>
				{Object.keys(mask).sort().map((value, index) => <option key={index} children={value} />)}
				<option children={"Base"} />
			</select>

			{current && Object.keys(current).map((value, index) => <Input key={index} name={value} />)}
		</div>
	);
}

const Input = ({ name }) => {
	const { buffer, eventbus } = useEventBus();
	const { currentMask, mask } = buffer;
	const requirements = mask[currentMask][name];
	const [value, setValue] = useState(0);

	const handlerChange = e => setValue(e.target.value);
	const handlerMouse = () => {
		eventbus.publish('buffer.transformation', buffer.currentMask, { [requirements.name]: value });
		console.log({ [requirements.name]: value });
	}

	useEffect(() => {
		setValue(requirements.value)
		// eslint-disable-next-line
	}, [buffer]);

	return (
		<div>
			<p>{name}: {value}</p>
			<input className="w-full" type={"range"} onChange={handlerChange} onInput={handlerChange} onMouseUp={handlerMouse}
				max={requirements.max}
				min={requirements.min}
				step={requirements.step} 
				value={value} />
		</div>
	);

}

export default PopupMask;