import dotenv from "dotenv";
import process from "node:process";

dotenv.config()

const envConfig = {
    PORT: process.env.PORT || 3000,
    NODE_ENV: process.env.NODE_ENV || "development",
    DEV: process.env.NODE_ENV === 'development',
    MONGODB_URI: process.env.MONGODB_URI || 'mongodb://localhost:27017/diary',
    JWT_SECRET: process.env.JWT_SECRET,
    SECRET_KEY: process.env.SECRET_KEY,
    API_KEY: process.env.API_KEY,

    LOG_LEVEL: process.env.LOG_LEVEL || "info",

    CONSOLE_LOG: process.env.CONSOLE_LOG?.toString() === "1",
    CORS_WHITELIST: process.env.CORS_WHITELIST?.split(",") || [],
}


export default envConfig
