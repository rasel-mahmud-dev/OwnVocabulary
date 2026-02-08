import {createServer} from 'http';
import dotenv from 'dotenv';
import app from "./build/app";

dotenv.config();
const PORT = process.env.PORT || 3000;
const server = createServer(app);
server.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});

