import dotenv from 'dotenv';
dotenv.config();
import express from 'express';
import cors from 'cors';
import route from "./routes";
import morgan from "morgan";
import ratelimit from "express-rate-limit";
import envConfig from "./config/envConfig";

const app = express();
app.use(cors());
app.use(morgan("dev"))
app.use(express.json());

app.use(ratelimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    limit: envConfig.DEV ? 5000 : 300, // limit each IP to 100 requests per windowMs
    message: "Too many requests from this IP, please try again after a minute"
}))

// app.use(express.static(path.resolve("public/dist")));
// app.use("/assets/", express.static(path.resolve("public/dist/assets/")));
// app.use((req, res, next) => {
//     if (req?.originalUrl?.includes("/api")) {
//         return next()
//     }
//     res.sendFile(path.resolve("public/dist/index.html"));
// })

app.use(route)


app.use((err, req, res, next) => {
    const status = err.status || 500;
    const message = err.error || err?.message || 'Something went wrong';
    res.status(status || 500).json({message});
})

export default app