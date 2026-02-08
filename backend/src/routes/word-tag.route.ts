import { Router } from "express";
const router = Router();

import wordTagController from "../controllers/word-tag.controller";
import { checkApiKeys } from "../middlewares";

router.put('/api/v2/word-tag/:uid', checkApiKeys, wordTagController.updateWordTag);
router.get('/api/v2/word-tag/pull', checkApiKeys, wordTagController.wordTagPull);

export default router
