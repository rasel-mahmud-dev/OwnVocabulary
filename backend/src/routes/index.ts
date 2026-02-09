import express from "express";
import wordRoute from "./word.route";

const route = express.Router()
route.use(wordRoute)

export default route
