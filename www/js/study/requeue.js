/** Оценённая карточка уходит из очереди и в сессии больше не показывается (план составлен один раз при открытии; её новое расписание нужно следующему плану). */
export default function requeue(session) {
  session.order.splice(session.pos, 1);
  if (session.pos >= session.order.length) session.pos = 0;
}
