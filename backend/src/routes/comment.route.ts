import { Router } from "express";
const router = Router();

import commentController from "../controllers/comment.controller";
import { checkApiKeys } from "../middlewares";

router.get('/api/v2/comment', commentController.getComments);
router.put('/api/v2/comment/:uid', checkApiKeys, commentController.updateComment);
router.get('/api/v2/comment/pull', checkApiKeys, commentController.commentPull);

export default router
