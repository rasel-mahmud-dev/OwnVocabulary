import express from "express";
import wordRoute from "./word.route";
import tagRoute from "./tag.route";
import categoryRoute from "./category.route";
import commentRoute from "./comment.route";
import postCommentRoute from "./post-comment.route";
import wordTagRoute from "./word-tag.route";
import wordCategoryRoute from "./word-category.route";

const route = express.Router()
route.use(wordRoute)
route.use(tagRoute)
route.use(categoryRoute)
route.use(commentRoute)
route.use(postCommentRoute)
route.use(wordTagRoute)
route.use(wordCategoryRoute)

export default route
