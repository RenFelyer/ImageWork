
const CardLoad = () => {
	const handleFileChange = async (event) => {
		const file = event.target.files[0];
		const formData = new FormData();
		formData.append("file", file);

		fetch(`http://${process.env.REACT_APP_IP}:8393/upload`, {
			method: "POST",
			body: formData
		});
	};

	return (
		<div className={`flex-none w-[110px] min-h-[150px] rounded overflow-hidden border-2 border-black mr-2`}>
			<label>
				<div className="flex justify-center items-center cursor-pointer w-full h-full bg-black/80 font-bold text-white text-2xl" children="+" />
				<input type='file' accept="image/*" onChange={handleFileChange} hidden />
			</label>
		</div>
	)
}

export default CardLoad;