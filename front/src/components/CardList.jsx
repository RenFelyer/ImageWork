import useEventBus from "../hooks/useEventBus";
import CardItem from "./card/CardItem";
import CardLoad from "./card/CardLoad";

const CardList = () => {
	const {filename, filelist } = useEventBus();

	return (
		<div className="flex w-full pl-2 py-2 overflow-x-auto">
			{filelist.map((value, index) => <CardItem key={index} filename={value} isSelected={value === filename} />)}
			<CardLoad />
		</div >
	);
}

export default CardList;