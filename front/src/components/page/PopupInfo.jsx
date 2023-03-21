import Plot from "react-plotly.js";
import useEventBus from "../../hooks/useEventBus";

const PopupInfo = () => {
	const { buffer } = useEventBus();

	return (
		<div className="min-w-[240px] pt-2 px-2">
			<h2 className="text-center font-bold text-xl mb-4">Основні характеристики</h2>
			<Label name={'Cереднє значення інтенсивності'} value={buffer.meanIntensity} />
			<Label name={'Cереднє значення квадрата відхилення'} value={buffer.contrast} />
			<Label name={'Cередній інформаційний зміст '} value={buffer.entropy} />
			<Plot
				data={[{
					y:  buffer.histogram,
					type: "bar"
				}]}
				layout={{width: '100%', height: 400, }}
			/>
		</div>
	);
}


const Label = ({ name, value }) => {
	return name && (
		<div className="flex justify-between mb-2">
			<span className="mr-2">{name} = </span>
			<span>{value}</span>
		</div>
	);
}

export default PopupInfo;