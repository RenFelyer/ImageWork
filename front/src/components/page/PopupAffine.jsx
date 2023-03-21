import { useState } from "react";
import useEventBus from "../../hooks/useEventBus";

const PopupAffine = () => {
	const { affine } = useEventBus();


	return (
		<>
			<div className="min-w-[240px] pt-2 px-2">
				<h2 className="text-center font-bold text-xl mb-4">Афінне перетворення</h2>
				{Object.entries(affine).map(([key, value], index) => <Label key={index} name={key} defaultValue={value} />)}
			</div>
		</>
	);
}

const Label = ({ name }) => {
	const { affine, eventbus } = useEventBus();
	const [value, setValue] = useState(affine[name])

	const handlerChange = event => {
		const currentValue = event.target.value;
		if (currentValue.length <= 5) {
			setValue(currentValue);
			eventbus.publish('affine.update', currentValue.length > 0 ? currentValue : 0, { type: name });
		}
	}

	return name && (
		<div className="flex justify-between mb-2">
			<span>{name} = </span>
			<input className="border-b border-black outline-none" type='number' onChange={handlerChange} value={value} />
		</div>
	);
}

export default PopupAffine;