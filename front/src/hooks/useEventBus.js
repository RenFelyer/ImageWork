import { createContext, useContext, useEffect, useState } from "react";
import { useLoaderData } from "react-router-dom";


const eventBusContext = createContext({});

// eslint-disable-next-line
export default () => useContext(eventBusContext);

export const EBProvider = ({ children }) => {
	const { eventbus, list } = useLoaderData();
	const [filename, setFileName] = useState(list.image.filename);
	const [filelist, setFileList] = useState(list.image.filelist);
	const [affine, setAffine] = useState(list.affine);
	const [buffer, setBuffer] = useState(list.buffer);

	useEffect(() => {
		const handlerUpload = (_, message) => setFileList(value => value.concat(message.body))
		const handlerDelete = (_, message) => setFileList(value => value.filter(item => item !== message.body));
		const handlerSelected = (_, message) => setFileName(message.body);
		const handlerAffine = (_, message) =>  setAffine(value => ({ ...value, [message.headers.type]: Number(message.body.length > 0 ? message.body : 0) }));
		const handlerBuffer = (_, message) =>  setBuffer(message.body);

		eventbus.registerHandler('affine.update', handlerAffine)
		eventbus.registerHandler('image.upload', handlerUpload);
		eventbus.registerHandler('image.delete', handlerDelete);
		eventbus.registerHandler('image.selected', handlerSelected);
		eventbus.registerHandler('buffer.update.info', handlerBuffer);

		return () => {
			eventbus.unregisterHandler('affine.update', handlerAffine)
			eventbus.unregisterHandler('image.upload', handlerUpload);
			eventbus.unregisterHandler('image.delete', handlerDelete);
			eventbus.unregisterHandler('image.selected', handlerSelected);
			eventbus.unregisterHandler('buffer.update.info', handlerBuffer);
			eventbus.close();
		}
	}, [eventbus]);
	return <eventBusContext.Provider value={{ eventbus, filename, filelist, affine, buffer }} children={children} />
}