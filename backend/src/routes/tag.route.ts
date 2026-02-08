import { Router } from "express";
const router = Router();

import tagController from "../controllers/tag.controller";
import { checkApiKeys } from "../middlewares";

router.get('/api/v2/tag', tagController.getTags);
router.put('/api/v2/tag/:uid', checkApiKeys, tagController.updateTag);
router.get('/api/v2/tag/pull', checkApiKeys, tagController.tagPull);

export default router
