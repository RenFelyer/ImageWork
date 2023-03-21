import { useNavigate } from "react-router-dom";

const PopupMain = () => {
	return (
		<div className="px-2 pt-2">
			<Button name='Афінне перетворення' path="/Affine" />
			<Button name='Основні характеристики' path="/Info" />
			<Button name='Маски для зображення' path="/mask" />
			<ButtonExit />
		</div>
	);
}

const Button = ({ name = "none", path = "/" }) => {
	const navigate = useNavigate();
	return (<div className="bg-black/60 hover:bg-black/20 border border-black/60 mb-2 p-2 rounded-md cursor-pointer text-white" onClick={() => navigate(path)} children={name} />);
}

const ButtonExit = ({ name = "Завершити роботу сервера" }) => {
	return (<div className="bg-black/60 hover:bg-black/20 border border-black/60 mb-2 p-2 rounded-md cursor-pointer text-white"
		onClick={() => {
			fetch(`http://${process.env.REACT_APP_IP}:8393/shutdown`);
			window.close()
		}} children={name} />);
}

export default PopupMain;