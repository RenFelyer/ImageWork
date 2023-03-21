import { Outlet } from "react-router-dom";
import CardList from "./components/CardList";
import Main from "./components/Main";
import { EBProvider } from "./hooks/useEventBus";

const App = () => (
	<div className="flex flex-col h-full select-none">
		<EBProvider>
			<Main />
			<CardList />
			<Outlet />
		</EBProvider>
	</div>
)

export default App;