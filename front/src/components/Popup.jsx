import { Outlet, useNavigate } from "react-router-dom";

const Popup = () => {
	const navigate = useNavigate();

	return (
		<div className="flex fixed items-center justify-center w-full h-full bg-black/60" onClick={() => navigate("/")}>
			<div className="rounded-md bg-white p-3" onClick={e => e.stopPropagation()} children={<Outlet />} />
		</div>
	);
}

export default Popup;