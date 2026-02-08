import { Router } from "express";
const router = Router();

import wordCategoryController from "../controllers/word-category.controller";
import { checkApiKeys } from "../middlewares";

router.put('/api/v2/word-category/:uid', checkApiKeys, wordCategoryController.updateWordCategory);
router.get('/api/v2/word-category/pull', checkApiKeys, wordCategoryController.wordCategoryPull);

export default router
