import { Router } from "express";
const router = Router();

import wordController from "../controllers/word.controller";
import {checkApiKeys} from "../middlewares";

router.get('/api/v2/word', wordController.getWords);
router.put('/api/v2/word/:uid', checkApiKeys, wordController.updateWord);
router.get('/api/v2/word/pull', checkApiKeys, wordController.wordPull);

export default router