import useEventBus from "../../hooks/useEventBus";

const CardItem = ({ filename, isSelected = false }) => {
	const { eventbus } = useEventBus();

	const handlerDelete = event => {
		event.stopPropagation();
		eventbus.publish('image.delete', filename);
	}

	return (
		<div className={`flex-none rounded overflow-hidden border-2 ${isSelected ? 'border-green-600' : 'border-black'} mr-2`}>
			<div className="relative cursor-pointer" onClick={() => eventbus.publish('image.selected', filename)}>
				<div className="absolute top-2 right-2 hover bg-red-600/60 hover:bg-red-600 rounded-full p-1" onClick={handlerDelete}>
					<svg xmlns="http://www.w3.org/2000/svg" className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
						<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d='M6 18L18 6M6 6l12 12'></path>
					</svg>
				</div>
				<img className="h-[150px] pointer-events-none" src={`http://${process.env.REACT_APP_IP}:8393/load/${filename}`} alt="placeholder" />
			</div>
		</div>
	)
};

export default CardItem;
