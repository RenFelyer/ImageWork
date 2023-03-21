import EventBus from "@vertx/eventbus-bridge-client.js";
import { createBrowserRouter, createRoutesFromElements, Navigate, Route } from "react-router-dom";
import App from "./App";
import PopupAffine from "./components/page/PopupAffine";
import PopupInfo from "./components/page/PopupInfo";
import PopupMain from "./components/page/PopupMain";
import PopupMask from "./components/page/PopupMask";
import Popup from "./components/Popup";

const mainLoader = async () => {
	const eventbus = await new Promise((resolve, reject) => {
		const eb = new EventBus(`http://${process.env.REACT_APP_IP}:8393/eventbus`);
		eb.enableReconnect(true);
		eb.onopen = () => resolve(eb);
		eb.onerror = (err) => reject(err);
		eb.onclose = () => reject(new Error('EventBus is closed.'));
	});

	const list = await fetch(`http://${process.env.REACT_APP_IP}:8393/info`).then(value => value.json());
	return { eventbus, list };
}

export default createBrowserRouter(createRoutesFromElements(
	<Route path="/" element={<App />} errorElement={<Navigate to='/' />} loader={mainLoader}>
		<Route index element={<div />} />
		<Route element={<Popup />}>
			<Route path="Main" element={<PopupMain />} />
			<Route path="Info" element={<PopupInfo />} />
			<Route path="mask" element={<PopupMask />} />
			<Route path="Affine" element={<PopupAffine />} />
		</Route>
	</Route>
));

